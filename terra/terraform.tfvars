# the azure environment
env = {
  subscription   = "9c48fe80-ff8f-4143-9e26-13df7a9030a5"
  location       = "switzerlandnorth"
  resource_group = "poc"
  sku            = "standard"
  tenant_id      = "6c4b4ee4-c16f-4d2d-94d8-59e17852a93f"
}

#------- network ------

# the main virtual network
vnet = {
  name = "poc-vnet"
  cidr = ["10.1.0.0/16"]
}

# the service runtime subnet
srt_subnet = {
  name = "service-runtime-subnet"
  cidr = ["10.1.0.0/24"]
}

# the service runtime subnet
apps_subnet = {
  name = "apps-subnet"
  cidr = ["10.1.1.0/24"]
}

# the gateway subnet
gw_subnet = {
  name = "gateway-subnet"
  cidr = ["10.1.2.0/24"]
}

# ------ spring app -------

# Spring app
spring_service = {
  name = "app-service"
  cidr = ["10.2.0.0/16", "10.3.0.0/16", "10.4.0.1/16"]
}

# ------ cosmos db -------

