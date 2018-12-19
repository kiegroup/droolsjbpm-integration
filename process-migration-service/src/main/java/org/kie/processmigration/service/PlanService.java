/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.processmigration.service;

import java.util.List;

import org.kie.processmigration.model.Plan;
import org.kie.processmigration.model.exceptions.PlanNotFoundException;

public interface PlanService {

    List<Plan> findAll();

    Plan get(Long id) throws PlanNotFoundException;

    Plan create(Plan plan);

    Plan update(Long id, Plan plan) throws PlanNotFoundException;

    Plan delete(Long id) throws PlanNotFoundException;

}
