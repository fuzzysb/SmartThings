/**
 *  TV-Bed Device Handler
 *
 *  Copyright 2016 Stuart Buchanan
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 * 03/04/2016 V1.0 Initial Release
 */
 
 
preferences {

}

metadata {
	definition (name: "TV-Bed", namespace: "fuzzysb", author: "Stuart Buchanan") {
			
        capability "Switch"
		capability "Actuator"
        capability "Refresh"
        capability "Polling"	
	}

	simulator {
		
	}

tiles(scale: 2) {
    multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4){
        tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
            attributeState "on", label:'${name}', action:"switch.off", icon:"st.Bedroom.bedroom7", backgroundColor:"#79b821"
            attributeState "off", label:'${name}', action:"switch.on", icon:"st.Bedroom.bedroom7", backgroundColor:"#c0c0c0"
        }
    }
        main "switch"
		details(["switch"])
	}
}


private parseResponse(resp) {
    log.debug("Executing parseResponse: "+resp.data)
    log.debug("Output status: "+resp.status)
    if(resp.status == 200) {
        log.debug("Executing parseResponse.successTrue")
        def id = resp.data.id
        def name = resp.data.name
        def connected = resp.data.connected
		def returnValue = resp.data.return_value	
    }else if(resp.status == 201){
        log.debug("Something was created/updated")
    }
}

private getDeviceDetails() {
def fullDni = device.deviceNetworkId
return fullDni
}

private sendCommand(method, args = []) {
	def DefaultUri = "https://api.particle.io"
    def cdni = getDeviceDetails().tokenize(':')
	def deviceId = cdni[0] 
	def token = cdni[1]
    def methods = [
		'startTVUp': [
					uri: "${DefaultUri}",
					path: "/v1/devices/${deviceId}/startTVUp",
					requestContentType: "application/json",
					query: [access_token: token],
                    body: args[0]
                    ],
		'startTVDown': [
					uri: "${DefaultUri}",
					path: "/v1/devices/${deviceId}/startTVDown",
					requestContentType: "application/json",
					query: [access_token: token],
                    body: args[0]
                    ],
	]

	def request = methods.getAt(method)
    
    log.debug "Http Params ("+request+")"
    
    try{
        log.debug "Executing 'sendCommand'"
        
        if (method == "startTVUp"){
        	log.debug "calling startTVUp Method"
            httpPost(request) { resp ->            
                parseResponse(resp)
            }
        }else if (method == "startTVDown"){
			log.debug "calling startTVDown Method"
            httpPost(request) { resp ->            
                parseResponse(resp)
            }
        }else{
            httpGet(request)
        }
    } catch(Exception e){
        log.debug("___exception: " + e)
    }
}


def on() {
	log.debug "Executing 'on'"
	upCommand()
}

def off() {
	log.debug "Executing 'off'"
	downCommand()
}

def upCommand(){
	log.debug "Executing 'sendCommand.setState'"
    def jsonbody = new groovy.json.JsonOutput().toJson(arg:"on")
    sendCommand("startTVUp",[jsonbody])
	createEvent(name: "switch", value: "on", descriptionText: "$device.displayName is on")
}

def downCommand(){
	log.debug "Executing 'sendCommand.setState'"
	def jsonbody = new groovy.json.JsonOutput().toJson(arg:"off")
	sendCommand("startTVDown",[jsonbody])
	createEvent(name: "switch", value: "off", descriptionText: "$device.displayName is off")
}


