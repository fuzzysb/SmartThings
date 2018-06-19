import groovy.json.JsonSlurper
metadata {
	definition (name: "HomeSeer HS3", namespace: "fuzzysb", author: "Stuart Buchanan") {
	capability "Sensor"
    capability "Actuator"
    command "api"    
	}
    
    preferences {
		input("ip", "text", title: "IP Address", description: "Your HS3 IP Address", required: true, displayDuringSetup: true)
		input("port", "number", title: "Port Number", description: "Your HS3 Port Number (Default:80)", defaultValue: "80", required: true, displayDuringSetup: true)
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
   	body = new String(descMap["body"].decodeBase64())
	def slurper = new JsonSlurper()
	result = slurper.parseText(body)
    log.debug result
    events << createEvent(name:"hubInfo", value:result)
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

def api(String HS3Command, String HS3DevId) {
	ipSetup()
	def cmdPath
	def hubAction
	switch (HS3Command) {
		case "on":
			cmdPath = "/JSON?request=controldevicebyvalue&ref=${HS3DevId}&value=100"
			log.debug "The Switch On Command was sent to HS3 Device ${HS3DevId}"
		break;
		case "off":
			cmdPath = "/JSON?request=controldevicebyvalue&ref=${HS3DevId}&value=0"
			log.debug "The Switch Off Command was sent to HS3 Device ID: ${HS3DevId}"
                break;
		case "home":
			cmdPath = "/JSON?request=controldevicebyvalue&ref=${HS3DevId}&value=1"
			log.debug "The Switch On Command was sent to HS3 Device ${HS3DevId}"
		break;
		case "away":
			cmdPath = "/JSON?request=controldevicebyvalue&ref=${HS3DevId}&value=0"
			log.debug "The Switch Off Command was sent to HS3 Device ID: ${HS3DevId}"
                break;
                case "open":
			cmdPath = "/JSON?request=controldevicebyvalue&ref=${HS3DevId}&value=1"
			log.debug "The Contact Open Command was sent to HS3 Device ID: ${HS3DevId}"
                break;
		case "closed":
			cmdPath = "/JSON?request=controldevicebyvalue&ref=${HS3DevId}&value=0"
			log.debug "The Contact Closed Command was sent to HS3 Device ID: ${HS3DevId}"
                break;
                case "active":
			cmdPath = "/JSON?request=controldevicebyvalue&ref=${HS3DevId}&value=255"
			log.debug "The Motion active Command was sent to HS3 Device ID: ${HS3DevId}"
                break;
                case "inactive":
			cmdPath = "/JSON?request=controldevicebyvalue&ref=${HS3DevId}&value=0"
			log.debug "The Motion inactive Command was sent to HS3 Device ID: ${HS3DevId}"
		break;
	}
    
	switch (HS3Command) {
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