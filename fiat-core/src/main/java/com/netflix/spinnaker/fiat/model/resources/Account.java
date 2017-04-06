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

package com.netflix.spinnaker.fiat.model.resources;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netflix.spinnaker.fiat.model.Authorization;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Data
@NoArgsConstructor
// Jackson seems to prefer the all args constructor when available, but passes null for the
// Permissions object for 'legacy' objects with requiredGroupMembership.
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Account extends BaseAccessControlled<Account> implements Viewable {
  final ResourceType resourceType = ResourceType.ACCOUNT;

  private String name;
  private String cloudProvider;
  private Permissions.Mutable permissions = new Permissions.Mutable();
  private Set<Authorization> authorizations = new HashSet<>();

  @Override
  public Account cloneWithoutAuthorizations() {
    return new Account(name, cloudProvider, permissions, new HashSet<>());
  }

  @JsonIgnore
  public View getView() {
    return new View(this);
  }

  @Data
  @EqualsAndHashCode(callSuper = false)
  @NoArgsConstructor
  public static class View extends BaseView implements Authorizable {
    String name;
    Set<Authorization> authorizations;

    public View(Account account) {
      this.name = account.name;
      this.authorizations = account.authorizations;
    }
  }
}
