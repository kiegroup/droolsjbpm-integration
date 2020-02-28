/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.services.taskassigning.planning.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IndexedElementTest {

    @Test
    public void addInOrder() {
        String element1 = "1";
        String element2 = "2";
        String element3 = "3";
        String element4 = "4";
        String element5 = "5";
        String element6 = "6";
        String element7 = "7";

        List<IndexedElement<String>> elements = new ArrayList<>();

        IndexedElement.addInOrder(elements, new IndexedElement<>(element1, 4, true));
        IndexedElement.addInOrder(elements, new IndexedElement<>(element2, 1, true));
        IndexedElement.addInOrder(elements, new IndexedElement<>(element3, -1, true));
        IndexedElement.addInOrder(elements, new IndexedElement<>(element4, 2, false));
        IndexedElement.addInOrder(elements, new IndexedElement<>(element5, 3, true));
        IndexedElement.addInOrder(elements, new IndexedElement<>(element6, -1, false));
        IndexedElement.addInOrder(elements, new IndexedElement<>(element7, 1, false));

        assertEquals(element2, elements.get(0).getElement());
        assertEquals(element5, elements.get(1).getElement());
        assertEquals(element1, elements.get(2).getElement());
        assertEquals(element3, elements.get(3).getElement());
        assertEquals(element7, elements.get(4).getElement());
        assertEquals(element4, elements.get(5).getElement());
        assertEquals(element6, elements.get(6).getElement());
    }
}
