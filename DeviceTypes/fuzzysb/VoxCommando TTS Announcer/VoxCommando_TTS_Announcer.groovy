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
* 	Date 2016-03-028 v1.0 Initial Release
**/ 
preferences {
	input("ServerIP", "text", title: "ServerIP", description: "Enter the IP Address of the Voxcommando Web Server")
	input("Port", "text", title: "Port", description: "Enter the TCP Port of the Voxcommando Web Server")
}  

 
metadata {
	definition (name: "VoxCommando TTS Announcer", namespace: "fuzzysb", author: "Stuart Buchanan") {

		capability "Actuator"
		capability "Refresh"
        command "announce"
		command "creategroup"
		command "dissolveallgroups"
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
        def vlcState = data.state
        //LOG("VLC state: ${vlcState})")
        events << createEvent(name:"status", value:vlcState)
        if (vlcState == 'stopped') {
            events << createEvent([name:'trackDescription', value:''])
        }
    }

    if (data.containsKey('volume')) {
        //LOG("VLC volume: ${data.volume})")
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
	log.debug "Executing Test Message"
	announce("this is a test message",60)  
}

def announce(message,vol){
log.debug "Checking DNI"
updateDNI()

try {
	def cmds = []
     /*
    log.debug "Executing dissolvegroup"
	def dissolveGroupResult = dissolvegroup()
	cmds << sendHubCommand(dissolveGroupResult)
   
	log.debug "Executing creategroup"
	def createGroupResult = creategroup()
	cmds << sendHubCommand(createGroupResult)
    */
    
	log.debug "Executing selectplayer"
	def selectPlayerResult = selectplayer()
	cmds << sendHubCommand(selectPlayerResult)

	log.debug "Executing setvolume with: ${vol}"
	def setVolumeResult = setvolume(vol)
	cmds << sendHubCommand(setVolumeResult)
    
    message = URLEncoder.encode(message)
	log.debug "Executing ttsplay with the message: ${message}"
	def ttsResult = ttsplay(message)
	cmds << sendHubCommand(ttsResult)
   
    delayBetween(cmds,900)
    
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
        path: "/api/Sonos.CreateGroup&&Conservatory&&Conservatory&&Master%20Bedroom&&Kitchen",
        headers: headers
	
    )
    return result
}

def ttsplay(message){
    def headers = [:]
	headers.put("HOST","${settings.ServerIP}:${settings.Port}")
    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/api/Sonos.TTS.Speak&&,+${message}",
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
        //path: "/api/Sonos.SetPlayer&&Conservatory",
        path: "/api/Sonos.SetPlayer&&Conservatory%20%2B%20Master%20Bedroom%20%2B%20Kitchen",
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
