#
# terraform init
# terraform plan -out=deploy.plan
# terraform apply deploy.plan
#
# Optional: terraform destroy -auto-approve
#

# Azure provider version
terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "= 3.21.1"
    }
  }
}

provider "azurerm" {
  features {}
}

# Create Resource group
resource "azurerm_resource_group" "sc_corp_rg" {
  name     = var.env.resource_group
  location = var.env.location
}

#------- network --------

# Create Virtual Network
resource "azurerm_virtual_network" "sc_vnet" {
  name                = var.vnet.name
  address_space       = var.vnet.cidr
  location            = var.env.location
  resource_group_name = var.env.resource_group
  depends_on          = [azurerm_resource_group.sc_corp_rg]
}

# Create service runtime subnet
resource "azurerm_subnet" "srt_subnet" {
  name                 = var.srt_subnet.name
  resource_group_name  = var.env.resource_group
  virtual_network_name = var.vnet.name
  address_prefixes     = var.srt_subnet.cidr
  depends_on           = [azurerm_virtual_network.sc_vnet]
}

# Create app subnet
resource "azurerm_subnet" "app_subnet" {
  name                 = var.apps_subnet.name
  resource_group_name  = var.env.resource_group
  virtual_network_name = var.vnet.name
  address_prefixes     = var.apps_subnet.cidr
  depends_on           = [azurerm_subnet.srt_subnet]
}

# Create gateway subnet
resource "azurerm_subnet" "gateway_subnet" {
  name                 = var.gw_subnet.name
  resource_group_name  = var.env.resource_group
  virtual_network_name = var.vnet.name
  address_prefixes     = var.gw_subnet.cidr
  depends_on           = [azurerm_subnet.app_subnet]
}

#------- analytics --------

# Create Analytics WS
resource "azurerm_log_analytics_workspace" "law" {
  name                = "nahsi-analytics"
  resource_group_name = var.env.resource_group
  location            = var.env.location
  sku                 = "PerGB2018"
  retention_in_days   = 30
  depends_on          = [azurerm_subnet.gateway_subnet]
}

#------- vault --------

# create a key vault
resource "azurerm_key_vault" "vault" {

  name                          = "nahsi-poc-vault"
  location                      = var.env.location
  resource_group_name           = var.env.resource_group
  tenant_id                     = var.env.tenant_id
  purge_protection_enabled      = false
  public_network_access_enabled = true
  soft_delete_retention_days    = 7
  enable_rbac_authorization     = true
  sku_name                      = var.env.sku

  depends_on = [azurerm_log_analytics_workspace.law]
}

# get current user id
data "azurerm_client_config" "current" {
}

# allow me to manage the vault to manage secrets
resource "azurerm_role_assignment" "vault-admin" {
  scope                = azurerm_key_vault.vault.id
  role_definition_name = "Key Vault Administrator"
  principal_id         = data.azurerm_client_config.current.object_id

  depends_on = [azurerm_key_vault.vault]
}

#------- spring app --------

# assign roles to the spring app service
resource "azurerm_role_assignment" "vnet_roles" {

  for_each = var.vnet_roles

  scope                = azurerm_virtual_network.sc_vnet.id
  role_definition_name = each.key
  principal_id         = each.value

  depends_on = [azurerm_role_assignment.vault-admin]
}

# Create Spring Cloud Service
resource "azurerm_spring_cloud_service" "sc" {

  name                = var.spring_service.name
  resource_group_name = var.env.resource_group
  location            = var.env.location
  sku_name            = "S0"

  network {
    app_subnet_id             = "/subscriptions/${var.env.subscription}/resourceGroups/${var.env.resource_group}/providers/Microsoft.Network/virtualNetworks/${var.vnet.name}/subnets/${var.apps_subnet.name}"
    service_runtime_subnet_id = "/subscriptions/${var.env.subscription}/resourceGroups/${var.env.resource_group}/providers/Microsoft.Network/virtualNetworks/${var.vnet.name}/subnets/${var.srt_subnet.name}"
    cidr_ranges               = var.spring_service.cidr
  }

  config_server_git_setting {
    uri   = "https://github.com/msmock/azure-testbed-config"
    label = "main"
  }

  timeouts {
    create = "60m"
    delete = "2h"
  }

  tags = var.tags

  depends_on = [azurerm_role_assignment.vnet_roles]
}

# create a user assigned identity for the app instance
resource "azurerm_user_assigned_identity" "poc-app-service-identity" {
  location            = var.env.location
  resource_group_name = var.env.resource_group
  name                = "poc-app-service"
  depends_on          = [azurerm_spring_cloud_service.sc]
}

# output the user assigned identity to be used in CLI deployment
output "assigned-id" {
  value       = azurerm_user_assigned_identity.poc-app-service-identity
  description = "the user assigned identity to be used for the app instance in CLI deployment."

  depends_on = [azurerm_user_assigned_identity.poc-app-service-identity]
}

# assign role 'Key Vault Secrets User' to the app service
resource "azurerm_role_assignment" "secrets-user" {

  scope                = azurerm_key_vault.vault.id
  role_definition_name = "Key Vault Secrets User"
  principal_id         = azurerm_user_assigned_identity.poc-app-service-identity.principal_id

  depends_on = [azurerm_user_assigned_identity.poc-app-service-identity]
}
