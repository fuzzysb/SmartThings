/**
*   Copyright 2015 Stuart Buchanan
* 
*   Licensed under the Apache License, Version 2.0 (the License); you may not use this file except
*   in compliance with the License. You may obtain a copy of the License at
* 
*       httpwww.apache.orglicensesLICENSE-2.0
* 
*   Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
*   on an AS IS BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
*   for the specific language governing permissions and limitations under the License.
* 
* 	Axis Gate Opener
* 
* 	Author Stuart Buchanan
* 	Date 2016-04-05 v1.0 Initial Release
**/ 
preferences {
	input("ServerIP", "text", title: "ServerIP", description: "Enter the IP Address of the Axis Server")
	input("Port", "text", title: "Port", description: "Enter the TCP Port of the Axis Server")
}  
 
metadata {
	definition (name: "Axis Gate Opener", namespace: "fuzzysb", author: "Stuart Buchanan") {
		capability "Switch"
        capability "Actuator"
		capability "Refresh"
		capability "Polling"
        command "open"
		command "close" 
		command "getopen"
		command "getclose" 
}


	// simulator metadata
simulator {
		// status messages

		// reply messages
}

tiles(scale: 2) {
    multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4){
        tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
            attributeState "on", label:'Open', action:"switch.off", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/Axis%20Gate%20Opener/GateOpen.png", backgroundColor:"#ffa81e"
            attributeState "off", label:'Closed', action:"switch.on", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/Axis%20Gate%20Opener/GateClosed.png", backgroundColor:"#79b821"
        }
    }
        main "switch"
		details(["switch"])
	}
}

def updated() {
    log.info "Axis Gate Opener Updated"
    state.dni = createDNI(settings.ServerIP, settings.Port)
    state.hostAddress = "${settings.ServerIP}, ${settings.Port}"
}

def parse(String message) {
    def msg = stringToMap(message)
    if (msg.containsKey("simulator")) {
        // simulator input
        return parseHttpResponse(msg)
    }

    if (!msg.containsKey("headers")) {
        log.error "No HTTP headers found in '${message}'"
        return null
    }

    // parse HTTP response headers
    def headers = new String(msg.headers.decodeBase64())
    def parsedHeaders = parseHttpHeaders(headers)
    //log.debug "parsedHeaders: ${parsedHeaders}"
    if (parsedHeaders.status != 200) {
        log.error "Return Code: ${parsedHeaders.status} Server error: ${parsedHeaders.reason}"
        return null
    }

    // parse HTTP response body
    if (!msg.body) {
        log.error "No HTTP body found in '${message}'"
        return null
    }
	/*
    def body = new String(msg.body.decodeBase64())
    //log.debug "body: ${body}"
    body = body.replace("&", "&amp;")
    def result = new XmlParser()
    return parseHttpResponse(result.parseText(body.toString()))
	*/
}

private parseHttpHeaders(String headers) {
    def lines = headers.readLines()
    def status = lines.remove(0).split()

    def result = [
        protocol:   status[0],
        status:     status[1].toInteger(),
        reason:     status[2]
    ]

    return result
}

private def parseHttpResponse(Node data) {
    log.debug("parseHttpResponse(${data})")

    def events = []

    //log.debug "events: ${events}"
    return events
}


def on() {
	log.debug "Executing Open Command"
	open()
}

def off() {
	log.debug "Executing Close Command"
	close()
}

def open(){
log.debug "Checking DNI"
updateDNI()
try {
    log.debug "Executing Open"
	def openResult = getopen()
    log.debug "${openResult}"
	sendHubCommand(openResult)
	createEvent(name: "switch", value: "on", descriptionText: "$device.displayName is open", isStateChange: "true")
	}
	catch (Exception e)
	{
   		log.debug "Hit Exception $e"
	}
}

def close(){
log.debug "Checking DNI"
updateDNI()
try {
    log.debug "Executing Close"
	def closeResult = getclose()
    log.debug "${closeResult}"
	sendHubCommand(closeResult)
	createEvent(name: "switch", value: "off", descriptionText: "$device.displayName is closed", isStateChange: "true")
	}
	catch (Exception e)
	{
   		log.debug "Hit Exception $e"
	}
}


def getopen(){
    def headers = [:]
	headers.put("HOST","${settings.ServerIP}:${settings.Port}")
    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/axis-cgi/io/port.cgi?action=2:/",
        headers: headers
	
    )
    return result
}

def getclose(){
    def headers = [:]
	headers.put("HOST","${settings.ServerIP}:${settings.Port}")
    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/axis-cgi/io/port.cgi?action=2:\\",
        headers: headers
	
    )
    return result
}


private String createDNI(ipaddr, port) { 
    log.debug("createDNI(${ipaddr}, ${port})")

    def hexIp = ipaddr.tokenize('.').collect {
        String.format('%02X', it.toInteger())
    }.join()

    def hexPort = String.format('%04X', port.toInteger())
	log.debug "Hex IP:Port: ${hexIp}:${hexPort}"
    return "${hexIp}:${hexPort}"
}

private def delayHubAction(ms) {
    log.debug("delayHubAction(${ms})")
    return new physicalgraph.device.HubAction("delay ${ms}")
}

private updateDNI() { 
    if (device.deviceNetworkId != state.dni) {
        device.deviceNetworkId = state.dni
    }
}
