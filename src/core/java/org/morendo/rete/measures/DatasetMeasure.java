/*
 * Copyright 2002-2009 Jamocha
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://ruleml-dev.sourceforge.net/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.morendo.rete.measures;

import org.morendo.rete.Cube;
import org.morendo.rete.Rete;

import java.util.List;

public interface DatasetMeasure extends Measure {
    /**
     * Concrete implementation should return a list of objects that satisfy the query parameters
     *
     * @param engine
     * @param cube
     * @param parameters
     * @return
     */
    Object[] filterResults(Rete engine, Cube cube, List<?> data, List<?> parameters);
}
