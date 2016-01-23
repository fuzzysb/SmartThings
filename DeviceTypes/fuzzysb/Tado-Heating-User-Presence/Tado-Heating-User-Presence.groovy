/**
 *  Copyright 2015 Stuart Buchanan
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
 *	Tado Thermostat
 *
 *	Author: Stuart Buchanan, Based on original work by Ian M with thanks
 *	Date: 2015-12-04 v1.0 Initial Release
 */
 
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

preferences {
	input("username", "text", title: "Username", description: "Your Tado username")
	input("password", "password", title: "Password", description: "Your Tado password")
	input("tadouser", "text", title: "Tado User", description: "Your Tado User")
}  
 
metadata {
	definition (name: "Tado Heating User Presence", namespace: "fuzzysb", author: "Stuart Buchanan") {
		capability "Presence Sensor"
		capability "Sensor"
		capability "Polling"
		capability "Refresh"
        
        command "arrived"
		command "departed"
              
	}

	// simulator metadata
	simulator {
		status "present": "presence: present"
		status "not present": "presence: not present"
	}

	tiles {
		standardTile("presence", "device.presence", width: 2, height: 2, canChangeBackground: true) {
			state("not present", label:'not present', icon:"st.presence.tile.mobile-not-present", backgroundColor:"#ffffff", action:"arrived")
			state("present", label:'present', icon:"st.presence.tile.mobile-present", backgroundColor:"#53a7c0", action:"departed")
		}
		main "presence"
		details "presence"
	}
}

// Parse incoming device messages to generate events
private parseResponse(resp) {
    log.debug("Executing parseResponse: "+resp.data)
    log.debug("Output status: "+resp.status)
    if(resp.status == 200) {
    	log.debug("Executing parseResponse.successTrue")
        def evtJson = new groovy.json.JsonOutput().toJson(resp.data)
        def json = new JsonSlurper().parseText(evtJson)
		def appuserarray = new groovy.json.JsonOutput().toJson(json.appUsers)
        def list = new JsonSlurper().parseText(appuserarray)
		list.each {
    		if ((it.nickname).capitalize() == (settings.tadouser).capitalize()) {
            	log.debug("Found Tado User : " + it.nickname)
                if (it.geoTrackingEnabled == true) {
                	log.debug("Users GeoTracking is Enabled")
                	if (it.geolocationIsStale == false){
                    	log.debug("Users Current Relative Position is : " + it.relativePosition )
                		if (it.relativePosition == 0) {
                  		  	arrived()
                  		}else{
                  		  	departed()
                 	   }
                	}else{
                	log.debug("Geolocation is Stale Skipping")
                	}
                }else{
                	log.debug("Users GeoTracking Not Enabled")
                }
		}  
        }
    }else if(resp.status == 201){
        log.debug("Something was created/updated")
    }
}


def poll() {
	log.debug "Executing 'poll'"
	refresh()
}

def refresh() {
	log.debug "Executing 'refresh'"
    statusCommand()   
}


private sendCommand(method, args = []) {
    def methods = [
		'status': [
        			uri: "https://my.tado.com", 
                    path: "/mobile/1.6/getAppUsersRelativePositions", 
                    requestContentType: "application/json", 
                    query: [username:settings.username, password:settings.password]
                    ],
	]

	def request = methods.getAt(method)
    
    log.debug "Http Params ("+request+")"
    
    try{
        log.debug "Executing 'sendCommand'"
        
        if (method == "status"){
            httpGet(request) { resp ->            
                parseResponse(resp)
            }
        }else{
            httpGet(request)
        }
    } catch(Exception e){
        debug("___exception: " + e)
    }
}



// Commands
def statusCommand(){
	log.debug "Executing 'sendCommand.statusCommand'"
	sendCommand("status",[])
}

def arrived() {
	log.trace "Executing 'arrived'"
	sendEvent(name: "presence", value: "present")
}


def departed() {
	log.trace "Executing 'departed'"
	sendEvent(name: "presence", value: "not present")
}

