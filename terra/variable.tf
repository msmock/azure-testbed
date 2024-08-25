
## terraform init
## terraform plan -out=deploy.plan
## terraform apply deploy.plan
##
## Optional: terraform destroy -auto-approve


# the azure environment
variable "env" {}

# the main virtual network
variable "vnet" {}

# Spring Cloud runtime Service (e.g snet-runtime)
variable "srt_subnet" {}

# Spring applications subnet (e.g apps-runtime)
variable "apps_subnet" {}

# Gateway subnet
variable "gw_subnet" {}

# Spring app
variable "spring_service" {}

# ------- roles --------

# the required roles of the spring app container
variable "vnet_roles" {
    type = map(string)
    default = {
        "User Access Administrator" = "1e26bbb3-02a9-4b14-8812-7d3f0a632f1c",
        "Network Contributor"       = "1e26bbb3-02a9-4b14-8812-7d3f0a632f1c",
        # "Owner"                     = "e8de9221-a19c-4c81-b814-fd37c6caf9d2"
    }
    description = "Roles the spring app needs on the vnet"
}

# the required roles of the spring app container
variable "vault_roles" {
    type = map(string)
    default = {
        "Key Vault Secrets User" = "4633458b-17de-408a-b874-0445c86b69e6"
    }
    description = "Roles the spring app needs on the vault"
}


variable "tags" {
    type = map
    default = {
        environment = "Dev"
        BusinesUnit = "nahsi Service"
    }
    description = "key=value pairs to be applied as Tags on all resources which support tags"
}

