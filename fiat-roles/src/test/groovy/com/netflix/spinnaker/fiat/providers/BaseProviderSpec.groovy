/*
 * Copyright 2016 Google, Inc.
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

package com.netflix.spinnaker.fiat.providers

import com.netflix.spinnaker.fiat.model.Authorization
import com.netflix.spinnaker.fiat.model.resources.Resource
import com.netflix.spinnaker.fiat.model.resources.ResourceType
import com.netflix.spinnaker.fiat.model.resources.Role
import groovy.transform.EqualsAndHashCode
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import org.apache.commons.collections4.CollectionUtils
import spock.lang.Specification
import spock.lang.Subject

class BaseProviderSpec extends Specification {

  private static Authorization R = Authorization.READ
  private static Authorization W = Authorization.WRITE

  TestResource noReqGroups
  TestResource reqGroup1
  TestResource reqGroup1and2

  def setup() {
    noReqGroups = new TestResource()
        .setName("noReqGroups")
    reqGroup1 = new TestResource()
        .setName("reqGroup1")
        .setPermissions(new Resource.Permissions().add(R, "group1"))
    reqGroup1and2 = new TestResource()
        .setName("reqGroup1and2")
        .setPermissions(new Resource.Permissions().add(R, "group1")
                                                  .add(W, "group2"))
  }

  def "should get all unrestricted"() {
    setup:
    @Subject provider = new TestResourceProvider()

    when:
    provider.all = [noReqGroups]
    def result = provider.getAllUnrestricted()

    then:
    result.size() == 1
    def expected = noReqGroups.cloneWithoutAuthorizations().setAuthorizations([R, W] as Set)
    result.first() == expected

    when:
    provider.all = [reqGroup1]
    result = provider.getAllUnrestricted()

    then:
    result.isEmpty()
  }

  def "should get restricted"() {
    setup:
    @Subject provider = new TestResourceProvider()

    when:
    provider.all = [noReqGroups]
    def result = provider.getAllRestricted([new Role("group1")])

    then:
    result.isEmpty()

    when:
    provider.all = [reqGroup1]
    result = provider.getAllRestricted([new Role("group1")])

    then:
    result.size() == 1
    CollectionUtils.disjunction(result.first().authorizations, [R]).isEmpty()

    when:
    provider.all = [reqGroup1and2]
    result = provider.getAllRestricted([new Role("group1")])

    then:
    result.size() == 1
    CollectionUtils.disjunction(result.first().authorizations, [R]).isEmpty()
    result.first().authorizations.clear()

    when: "use additional groups that grants additional authorizations."
    result = provider.getAllRestricted([new Role("group1"), new Role("group2")])

    then:
    result.size() == 1
    CollectionUtils.disjunction(result.first().authorizations,
                                [R, W]).isEmpty()
    result.first().authorizations.clear()

    when:
    provider.getAllRestricted(null)

    then:
    thrown IllegalArgumentException
  }

  def "should start and remain unhealthy until first success"() {
    setup:
    @Subject provider = new TestResourceProvider(unhealthyThreshold: 2)

    expect:
    !provider.isProviderHealthy()
    provider.failure()
    !provider.isProviderHealthy()

    provider.success()
    provider.isProviderHealthy()

    provider.failure()
    provider.isProviderHealthy()
    provider.failure()
    !provider.isProviderHealthy()

    provider.success()
    provider.isProviderHealthy()
  }

  class TestResourceProvider extends BaseProvider<TestResource> {
    Set<TestResource> all = new HashSet<>()
  }

  @Builder(builderStrategy = SimpleStrategy, prefix = "set")
  @EqualsAndHashCode
  class TestResource implements Resource.AccessControlled<TestResource> {
    final ResourceType resourceType = ResourceType.APPLICATION // Irrelevant for testing.
    String name
    Set<Authorization> authorizations = new HashSet<>()
    Resource.Permissions permissions = new Resource.Permissions()

    @Override
    TestResource cloneWithoutAuthorizations() {
      return new TestResource(name: name, permissions: permissions)
    }
  }
}
