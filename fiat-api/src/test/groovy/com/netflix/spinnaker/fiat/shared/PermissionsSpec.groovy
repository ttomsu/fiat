/*
 * Copyright 2017 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.fiat.shared

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.netflix.spinnaker.fiat.model.Authorization
import spock.lang.Specification

class PermissionsSpec extends Specification {

  private static final Authorization R = Authorization.READ
  private static final Authorization W = Authorization.WRITE


  ObjectMapper mapper = new ObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, true)

  String permissionJson = '''{
  "READ" : [ "foo" ],
  "WRITE" : [ "bar" ]
}'''

  def "should deserialize"() {
    when:
    Permissions p = mapper.readValue(permissionJson, Permissions)

    then:
    p.get(R) == ["foo"]
    p.get(W) == ["bar"]
  }

  def "should serialize"() {
    when:
    Permissions p = new Permissions([(R): ["foo"], (W): ["bar"]])

    then:
    permissionJson == mapper.writeValueAsString(p)

    when:
    p = p.asImmutable()

    then:
    permissionJson == mapper.writeValueAsString(p)
  }

  def "test immutability"() {
    setup:
    Permissions p = new Permissions([(R): ["foo"], (W): ["bar"]])

    when:
    p.add(R, "baz")

    then:
    p.get(R).size() == 2

    when:
    p = p.asImmutable()
    p.add(W, "bar")

    then:
    thrown UnsupportedOperationException
  }
}
