import groovy.json.JsonSlurper
metadata {
	definition (name: "Vera Plus", namespace: "fuzzysb", author: "Stuart Buchanan") {
	capability "Sensor"
    capability "Actuator"
    command "api"    
	}
    
    preferences {
		input("ip", "text", title: "IP Address", description: "Your Vera IP Address", required: true, displayDuringSetup: true)
		input("port", "number", title: "Port Number", description: "Your Vera Port Number (Default:3480)", defaultValue: "3480", required: true, displayDuringSetup: true)
	}
   
	tiles (scale: 2){      
        valueTile("hubInfo", "device.hubInfo", decoration: "flat", height: 2, width: 6, inactiveLabel: false, canChangeIcon: false) {
            state "hubInfo", label:'${currentValue}'
        }
    }
	main("hubInfo")
	details(["hubInfo"])
}

def parse(description) {
	log.debug description
	def events = [] 
    def result
   	def descMap = parseDescriptionAsMap(description)
    def body = new String(descMap["body"])
    if (body != "T0s="){
    	body = new String(descMap["body"].decodeBase64())
   		def slurper = new JsonSlurper()
   		result = slurper.parseText(body)
    } else {
    	result = "OK"
    }
    log.debug result
    if (result == "OK"){
    	events << createEvent(name:"hubInfo", value:result)
    }
    else (result.containsKey("OK")) {
       events << createEvent(name:"hubInfo", value:result.OK)
    }
    return events
}

def parseDescriptionAsMap(description) {
	description.split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
        
        if (nameAndValue.length == 2) map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
        else map += [(nameAndValue[0].trim()):""]
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	initialize()
}

def initialize() {
	ipSetup()
}

def api(String veraCommand, String veraDevId) {
	ipSetup()
	def cmdPath
	def hubAction
	switch (veraCommand) {
		case "on":
			cmdPath = "/data_request?id=action&output_format=json&DeviceNum=${veraDevId}&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=1"
			log.debug "The Switch On Command was sent to Vera Device ${veraDevId}"
		break;
		case "off":
			cmdPath = "/data_request?id=action&output_format=json&DeviceNum=${veraDevId}&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=0"
			log.debug "The Switch Off Command was sent to Vera Device ID: ${veraDevId}"
        break;
        case "open":
			cmdPath = "/data_request?id=variableset&output_format=json&DeviceNum=${veraDevId}&serviceId=urn:micasaverde-com:serviceId:SecuritySensor1&Variable=Tripped&Value=1"
			log.debug "The Contact Open Command was sent to Vera Device ID: ${veraDevId}"
        break;
		case "closed":
			cmdPath = "/data_request?id=variableset&output_format=json&DeviceNum=${veraDevId}&serviceId=urn:micasaverde-com:serviceId:SecuritySensor1&Variable=Tripped&Value=0"
			log.debug "The Contact Closed Command was sent to Vera Device ID: ${veraDevId}"
        break;
        case "active":
			cmdPath = "/data_request?id=variableset&output_format=json&DeviceNum=${veraDevId}&serviceId=urn:micasaverde-com:serviceId:SecuritySensor1&Variable=Tripped&Value=1"
			log.debug "The Motion active Command was sent to Vera Device ID: ${veraDevId}"
        break;
        case "inactive":
			cmdPath = "/data_request?id=variableset&output_format=json&DeviceNum=${veraDevId}&serviceId=urn:micasaverde-com:serviceId:SecuritySensor1&Variable=Tripped&Value=0"
			log.debug "The Motion inactive Command was sent to Vera Device ID: ${veraDevId}"
		break;
	}
    
	switch (veraCommand) {
		default:
			try {
				hubAction = [new physicalgraph.device.HubAction(
				method: "GET",
				path: cmdPath,
				headers: [HOST: "${settings.ip}:${settings.port}", Accept: "application/json"]
				)]
			}
			catch (Exception e) {
				log.debug "Hit Exception $e on $hubAction"
			}
			break;
	}
	return hubAction
}

def ipSetup() {
    log.debug "In IPSetup Area"
	def hosthex
	def porthex
	if (settings.ip) {
		hosthex = convertIPtoHex(settings.ip).toUpperCase()
	}
	if (settings.port) {
		porthex = convertPortToHex(settings.port).toUpperCase()
	}
	if (settings.ip && settings.port) {
        log.debug "updating Network ID to ${hosthex}:${porthex}"
		device.deviceNetworkId = "${hosthex}:${porthex}"
	}
}

private String convertIPtoHex(ip) { 
	String hexip = ip.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
	return hexip
}
private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
	return hexport
}
