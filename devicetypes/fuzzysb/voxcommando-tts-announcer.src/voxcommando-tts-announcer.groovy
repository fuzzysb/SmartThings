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
* 	VoxCommando TTS Announcer
* 
* 	Author Stuart Buchanan
* 	Date 2016-04-04 v1.0 updated with new options in latest vox commando beta
* 	Date 2016-03-28 v1.0 Initial Release
**/ 
preferences {
	input("ServerIP", "text", title: "ServerIP", description: "Enter the IP Address of the Voxcommando Web Server")
	input("Port", "text", title: "Port", description: "Enter the TCP Port of the Voxcommando Web Server")
    input("DefaultVolume", "number", title: "Default Announcement Volume", description: "Enter the Default Volume % for Announcements")
    /*
	input("MasterSonos", "text", title: "Master Sonos Device", description: "Enter the name of the groups Master Sonos Player")
    input("SecondSonos", "text", title: "Second Sonos Device", description: "Enter the name of the second Sonos Player")
    input("ThirdSonos", "text", title: "Third Sonos Device", description: "Enter the name of the third Sonos Player")
    input("FourthSonos", "text", title: "Fourth Sonos Device", description: "Enter the name of the third Sonos Player")
    */
}  
 
metadata {
	definition (name: "VoxCommando TTS Announcer", namespace: "fuzzysb", author: "Stuart Buchanan") {
		capability "Speech Synthesis"
        capability "Actuator"
		capability "Refresh"
        command "announce"
		command "creategroup"
        command "getgroup"
		command "dissolveallgroups"
        command "dissolvegroup"
        command "nullplay"
		command "ttsplay"
		command "selectplayer"
		command "setvolume"
		command "test"      
}


	// simulator metadata
simulator {
		// status messages

		// reply messages
}

tiles(scale: 2){
    standardTile("Test", "device.switch", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
		state "default", label:"Test", action:"test", icon:"st.Entertainment.entertainment3"
		}
	
	
		main("Test")
		details(["Test"])
	}
}

def updated() {
    log.info "VoxCommando TTS Announcer Updated"
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

    def body = new String(msg.body.decodeBase64())
    //log.debug "body: ${body}"
    body = body.replace("&", "&amp;")
    def result = new XmlParser()
    return parseHttpResponse(result.parseText(body.toString()))
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
/*
    if (data.containsKey('state')) {
        def vcState = data.state
        //LOG("VLC state: ${vcState})")
        events << createEvent(name:"status", value:vcState)
        if (vcState == 'stopped') {
            events << createEvent([name:'trackDescription', value:''])
        }
    }

    if (data.containsKey('volume')) {
        //LOG("VC volume: ${data.volume})")
        def volume = ((data.volume.toInteger() * 100) / 512) as int
        events << createEvent(name:'level', value:volume)
    }

    if (data.containsKey('information')) {
        parseTrackInfo(events, data.information)
    }
*/
    //log.debug "events: ${events}"
    return events
}


def test() {
	def defvol = settings.DefaultVolume ?: 70
	log.debug "Executing Test Message"
	announce("this is a test message",defvol)  
}

def announce(message,vol){
log.debug "Checking DNI"
updateDNI()

try {
    /* 
    log.debug "Executing dissolvegroup"
	def dissolveGroupResult = dissolvegroup()
	sendHubCommand(dissolveGroupResult)
    delayHubAction(1000)
    
	log.debug "Executing creategroup"
	def createGroupResult = creategroup()
	sendHubCommand(createGroupResult)
    delayHubAction(1000)
    */
    
	log.debug "Executing selectplayer"
	def selectPlayerResult = selectplayer()
	sendHubCommand(selectPlayerResult)
    delayHubAction(500)
    /*
    log.debug "Executing GetGroupPlayer"
	def getGroupResult = getgroup()
	sendHubCommand(getGroupResult)
    delayHubAction(500)
	*/

    message = URLEncoder.encode(message)
	log.debug "Executing ttsplay with the message: ${message} at Volume Level ${vol}"
	def ttsResult = ttsplay(message, vol)
    sendHubCommand(ttsResult)
	}
	catch (Exception e)
	{
   		log.debug "Hit Exception $e"
	}
}

def creategroup(){
    def headers = [:]
	headers.put("HOST","${settings.ServerIP}:${settings.Port}")
    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/api/Sonos.CreateGroup&&Conservatory&&Master+Bedroom&&Kitchen",
        headers: headers
	
    )
    return result
}

def getgroup(){
    def headers = [:]
	headers.put("HOST","${settings.ServerIP}:${settings.Port}")
    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/api/Sonos.GetGroupInfo",
        headers: headers
    )
    return result
}

def ttsplay(message, vol){
    def headers = [:]
	headers.put("HOST","${settings.ServerIP}:${settings.Port}")
    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/api/Sonos.TTS.Speak&&,+${message}&&${vol}",
		headers: headers
    )
	//log.debug result
    return result
}


def nullplay(){
    def headers = [:]
	headers.put("HOST","${settings.ServerIP}:${settings.Port}")
    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/api/Sonos.TTS.Speak&&,",
		headers: headers
    )
	//log.debug result
    return result
}


def selectplayer(){
    def headers = [:]
	headers.put("HOST","${settings.ServerIP}:${settings.Port}")
    def result = new physicalgraph.device.HubAction(
        method: "GET",
        //path: "/api/Sonos.SetPlayer&&Conservatory%20%2B%20Master%20Bedroom%20%2B%20Kitchen",
        path: "/api/Sonos.SetPlayer&&Conservatory",
		headers: headers
    )
    return result
}

def setvolume(vol){
    def headers = [:]
	headers.put("HOST","${settings.ServerIP}:${settings.Port}")
    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/api/Sonos.Player.SetGrpVol&&${vol}",
		headers: headers
    )
    return result
}

def speak(message){
	def defvol = settings.DefaultVolume ?: 70
	log.debug "Executing announcement with: ${message} at Volume: ${defvol}"
	announce("${message}",defvol)
}

def dissolvegroup(){
    def headers = [:]
	headers.put("HOST","${settings.ServerIP}:${settings.Port}")
    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/api/Sonos.DissolveGroup&&Conservatory&&Master+Bedroom&&Kitchen",
		headers: headers
    )
    return result
}


def dissolveallgroups(){
    def headers = [:]
	headers.put("HOST","${settings.ServerIP}:${settings.Port}")
    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/api/Sonos.DissolveAllGroups",
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
