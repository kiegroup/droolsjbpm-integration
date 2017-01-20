/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.router.proxy.aggragate;

import static org.kie.server.router.utils.Helper.readProperties;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONResponseAggregator implements ResponseAggregator {

    private static final String JSON_TYPE = "application/json";

    private static final Properties sortByMapping = readProperties(JSONResponseAggregator.class.getResourceAsStream("/sort-json.mapping"));

    public String aggregate(List<String> data) {

        return aggregate(data, null, true, 0, 10);
    }


    @Override
    public String aggregate(List<String> data, String sortBy, boolean ascending, Integer page, Integer pageSize) {

        try {
            JSONObject json = data.stream().map(s -> {
                return newJson(s);
            }).reduce((source, target) -> {
                deepMerge(source, target);
                return target;
            }).get();

            String response = sort(sortBy, ascending, page, pageSize, json);

            return response;
        } catch (IllegalArgumentException e) {
            // try with sorting array

            JSONArray jsonArray = data.stream().map(s -> {
                return newJsonArray(s);
            }).reduce((source, target) -> {
                deepMergeArray(source, target);
                return target;
            }).get();

            String response = sortArray(sortBy, ascending, page, pageSize, jsonArray);

            return response;
        }

    }

    protected String sort(String fieldName, boolean ascending, Integer page, Integer pageSize, JSONObject source) {
        try {
            for (String key: JSONObject.getNames(source)) {
                Object value = source.get(key);
                if (value instanceof JSONArray) {
                    JSONArray array = (JSONArray) value;
                    // apply sorting
                    sortList(fieldName, array, ascending, page, pageSize);
                }
            }

            return source.toString(2);
        } catch (Exception e) {
            throw new RuntimeException("Error while sorting and paging of json", e);
        }
    }

    protected JSONObject deepMerge(JSONObject source, JSONObject target) {
        try {
            for (String key: JSONObject.getNames(source)) {
                Object value = source.get(key);
                if (!target.has(key)) {
                    // new value for "key":
                    target.put(key, value);
                } else {
                    // existing value for "key" - recursively deep merge:
                    if (value instanceof JSONObject) {
                        JSONObject valueJson = (JSONObject)value;
                        deepMerge(valueJson, target.getJSONObject(key));
                    }
                    // insert each JSONArray's JSONObject in place
                    else if (value instanceof JSONArray) {
                        JSONArray jsonArray = ((JSONArray) value);
                        for (int i = 0, size = jsonArray.length(); i < size; i++) {
                            JSONObject objectInArray = jsonArray.getJSONObject(i);
                            ((JSONArray) target.get(key)).put(objectInArray);
                        }
                    } else {
                        target.put(key, value);
                    }
                }
            }
            return target;
        } catch (JSONException e) {
            return null;
        }
    }

    protected JSONArray deepMergeArray(JSONArray source, JSONArray target) {
        try {

            for (int i = 0, size = source.length(); i < size; i++) {
                JSONArray objectInArray = source.getJSONArray(i);
                target.put(objectInArray);
            }
            return target;
        } catch (JSONException e) {
            return null;
        }
    }

    protected String sortArray(String fieldName, boolean ascending, Integer page, Integer pageSize, JSONArray source) {
        try {
            // apply sorting
            sortList(fieldName, source, ascending, page, pageSize);

            return source.toString(2);
        } catch (Exception e) {
            throw new RuntimeException("Error while sorting and paging of json", e);
        }
    }

    protected JSONObject newJson(String data) {
        try {

            return new JSONObject(data);
        } catch (JSONException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    protected JSONArray newJsonArray(String data) {
        try {

            return new JSONArray(data);
        } catch (JSONException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    @Override
    public boolean supports(Object... acceptTypes) {
        for (Object acceptType : acceptTypes ) {
            if (acceptType == null) {
                continue;
            }
            boolean found = acceptType.toString().toLowerCase().contains(JSON_TYPE);
            if (found) {
                return true;
            }
        }

        return false;
    }

    protected void sortList(String fieldName, JSONArray array, boolean ascending, int page, int pageSize) throws Exception{

        Field f = array.getClass().getDeclaredField("myArrayList");
        f.setAccessible(true);
        List<?> jsonList = (List<?>) f.get(array);

        if (fieldName != null && !fieldName.isEmpty()) {
            String sortBy = sortByMapping.getProperty(fieldName, fieldName);


            Collections.sort(jsonList, new Comparator<Object>() {

                @SuppressWarnings({"rawtypes", "unchecked"})
                @Override
                public int compare(Object o1, Object o2) {
                    if (o1 instanceof JSONObject && o2 instanceof JSONObject) {
                        try {
                            Comparable v1 = (Comparable<?>)((JSONObject) o1).get(sortBy);
                            Comparable v2 = (Comparable<?>)((JSONObject) o2).get(sortBy);
                            if (ascending) {
                                return v1.compareTo(v2);
                            } else {
                                return v2.compareTo(v1);
                            }
                        } catch (Exception e) {

                        }


                    }
                    return 0;
                }
            });
        }
        // calculate paging
        int start = page * pageSize;
        int end = start + pageSize;
        // apply paging
        if (jsonList.size() < start) {
            // no elements in given range, return empty
            jsonList.clear();
        } else if (jsonList.size() >= end) {
            List<?> tmp = jsonList.subList(start, end);
            jsonList.retainAll(tmp);
        } else if (jsonList.size() < end) {
            List<?> tmp = jsonList.subList(start, jsonList.size());
            jsonList.retainAll(tmp);
        }
    }

}

