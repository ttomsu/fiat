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

package com.netflix.spinnaker.fiat.providers;

import com.netflix.spinnaker.fiat.model.resources.Account;
import com.netflix.spinnaker.fiat.providers.internal.ClouddriverService;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import retrofit.RetrofitError;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DefaultAccountProvider extends BaseProvider implements AccountProvider {

  @Autowired
  @Setter
  private ClouddriverService clouddriverService;

  @Override
  public Set<Account> getAll() throws ProviderException {
    try {
      val returnVal = clouddriverService.getAccounts().stream().collect(Collectors.toSet());
      success();
      return returnVal;
    } catch (RetrofitError re) {
      failure();
      throw new ProviderException(re);
    }
  }

  @Override
  public Set<Account> getAllRestricted(@NonNull Collection<String> groups) throws ProviderException {
    return getAll()
        .stream()
        .filter(account -> !Collections.disjoint(account.getRequiredGroupMembership(), groups))
        .collect(Collectors.toSet());
  }

  @Override
  public Set<Account> getAllUnrestricted() throws ProviderException {
    return getAll()
        .stream()
        .filter(account -> account.getRequiredGroupMembership().isEmpty())
        .collect(Collectors.toSet());
  }
}
