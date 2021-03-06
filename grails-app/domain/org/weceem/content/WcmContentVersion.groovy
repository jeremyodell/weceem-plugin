/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.weceem.content

/**
 * Class for storing versioning information and versioned object data.
 *
 * @author Sergei Shushkevich
 */
class WcmContentVersion {

    Integer revision
    Long objectKey
    String objectClassName
    String objectContent
    String contentTitle
    String spaceName
    String createdBy
    Date createdOn

    def updateRevisions() {
        // Keep past revisions up to date with the node's current info, in case node is deleted eventually
        WcmContentVersion.executeUpdate(
                "update WcmContentVersion cv set cv.contentTitle = ?, cv.spaceName = ? where cv.objectKey = ?",
                [contentTitle, spaceName, objectKey])
    }

    static constraints = {
        objectContent(maxSize:500000)
        revision(nullable: true)
        createdBy(nullable: true)
        createdOn(nullable: true)
    }

    static mapping = {
        objectContent type: 'text'
    }
    
    def extractProperties() {
        def slurper = new XmlSlurper()
        def doc = slurper.parseText(objectContent)
        def data = [content:null, properties:[:]]
        data.content = decodeXML(doc.content.text())
        doc.property.each { n ->
            data.properties[decodeXML(n.@name.text())] = decodeXML(n.text())
        }
        return data
    }
    
    def decodeXML(s) {
        // MUST escape & last
        s = s?.replaceAll('&lt;', '<')
        s = s?.replaceAll('&gt;', '>')
        s = s?.replaceAll('&amp;', '&')
        return s
    }
}