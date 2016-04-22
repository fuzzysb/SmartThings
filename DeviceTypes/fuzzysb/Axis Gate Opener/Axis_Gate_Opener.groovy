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
*   Date 2016-04-22 v1.6 Fixed issue with State CreateEvent not sending Value
*   Date 2016-04-22 v1.5 removed just carriage returns ot line feeds from the end of the string
*   Date 2016-04-22 v1.4 removed what looks to be a CR LF or /r/n from the end of the string
*   Date 2016-04-21 v1.3 added some extra debug logging around the status create event to figure out why the gates status is not updating correctly
* 	Date 2016-04-06 v1.2 should now correctly get gate status and handle state changes correctly
* 	Date 2016-04-06 v1.1 Added User Authentication, added logging of response parsed http headers.
* 	Date 2016-04-05 v1.0 Initial Release
**/ 
preferences {
	input("ServerIP", "text", title: "ServerIP", description: "Enter the IP Address of the Axis Server")
	input("Port", "text", title: "Port", description: "Enter the TCP Port of the Axis Server")
	input("username", "text", title: "username", description: "Enter the username for the Axis Server")
	input("password", "password", title: "password", description: "Enter the Password for the Axis Server")
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
	standardTile("open", "device.open", width: 2, height: 2, decoration: "flat") {
			state "default", label:"Open", action:"switch.on", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/Axis%20Gate%20Opener/GateOpen.png", backgroundColor:"#ffa81e"
		}	
	standardTile("close", "device.close", width: 2, height: 2, decoration: "flat") {
			state "default", label:"Close", action:"switch.off", icon:"https://raw.githubusercontent.com/fuzzysb/SmartThings/master/DeviceTypes/fuzzysb/Axis%20Gate%20Opener/GateClosed.png", backgroundColor:"#79b821"
		}
	standardTile("refresh", "device.refresh", width: 2, height: 2, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}			
        main "switch"
		details(["switch","open","close","refresh"])
	}
}

def updated() {
    log.info "Axis Gate Opener Updated"
    state.dni = createDNI(settings.ServerIP, settings.Port)
    state.hostAddress = "${settings.ServerIP}, ${settings.Port}"
	refresh()
}

def installed() {
    log.info "Axis Gate Opener Updated"
    state.dni = createDNI(settings.ServerIP, settings.Port)
    state.hostAddress = "${settings.ServerIP}, ${settings.Port}"
	refresh()
}

def parse(String message) {
    def msg = stringToMap(message)
    def result

    if (!msg.containsKey("headers")) {
        log.error "No HTTP headers found in '${message}'"
        return null
    }

    // parse HTTP response headers
    def headers = new String(msg.headers.decodeBase64())
    def parsedHeaders = parseHttpHeaders(headers)
    log.debug "parsedHeaders: ${parsedHeaders}"
    if (parsedHeaders.status != 200) {
        log.error "Return Code: ${parsedHeaders.status} Server error: ${parsedHeaders.reason}"
        return null
    }

    // parse HTTP response body
    if (!msg.body) {
        log.error "No HTTP body found in '${message}'"
        return null
    } else {
	def body = new String(msg.body.decodeBase64())
	result = parseHttpResponse(body)
	log.debug "body: ${body}"
	}
    return result
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

private def parseHttpResponse(String data) {
    log.debug("parseHttpResponse(${data})")
    def result
	def splitresponse = data.split("=")
    def port = splitresponse[0]
	def status = splitresponse[1]
	status = status.replace("\r","")
	status = status.replace("\n","")
	log.debug("Gate status: " + status + ".")
	if (status == "active"){
	    log.debug("sending gate open status")
		result = createEvent(name: "switch", value: "on", descriptionText: "$device.displayName is open", isStateChange: "true")
	} else if (status == "inactive"){
	    log.debug("sending gate closed status")
		result = createEvent(name: "switch", value: "off", descriptionText: "$device.displayName is closed", isStateChange: "true")
	} else {log.debug("switch status not found")}
    return result
}

def poll() {
	log.debug "Executing poll Command"
	refresh()
}

def refresh() {
	log.debug "Executing Refresh Command"
	getstatus()
}

def on() {
	log.debug "Executing Open Command"
	open()
	refresh()
}

def off() {
	log.debug "Executing Close Command"
	close()
	refresh()
}

def open(){
log.debug "Checking DNI"
updateDNI()
try {
    log.debug "Executing Open"
	def openResult = getopen()
    log.debug "${openResult}"
	sendHubCommand(openResult)
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
	}
	catch (Exception e)
	{
   		log.debug "Hit Exception $e"
	}
}

def getopen(){
	def userpassascii = "${settings.username}:${settings.password}"
    def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
    def headers = [:]
	headers.put("HOST","${settings.ServerIP}:${settings.Port}")
	headers.put("Authorization","${userpass}")
    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/axis-cgi/io/port.cgi?action=2:/",
        headers: headers
    )
    return result
}

def getclose(){
	def userpassascii = "${settings.username}:${settings.password}"
    def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
    def headers = [:]
	headers.put("HOST","${settings.ServerIP}:${settings.Port}")	
	headers.put("Authorization","${userpass}")
    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/axis-cgi/io/port.cgi?action=2:\\",
        headers: headers
    )
    return result
}

def getstatus(){
	def userpassascii = "${settings.username}:${settings.password}"
    def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
    def headers = [:]
	headers.put("HOST","${settings.ServerIP}:${settings.Port}")	
	headers.put("Authorization","${userpass}")
    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/axis-cgi/io/port.cgi?checkactive=2",
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
