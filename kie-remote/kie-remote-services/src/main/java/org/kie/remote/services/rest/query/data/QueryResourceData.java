/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.remote.services.rest.query.data;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.kie.api.task.model.Status;
import org.kie.remote.services.exception.KieRemoteServicesInternalError;
import org.kie.remote.services.rest.exception.KieRemoteRestOperationException;

public class QueryResourceData {

    // @formatter:off
    static final String [] generalQueryParams = {
        "processinstanceid", "processid",
        "deploymentid"
    };

    static final String [] generalQueryParamsShort = {
        "piid", "pid",
        "did"
    };

    public static final String [] taskQueryParams = {
        "taskid",
        "initiator", "stakeholder", "potentialowner", "taskowner", "businessadmin",
        "taskstatus"
    };

    public static final String [] taskQueryParamsShort = {
       "tid",
       "init", "stho", "po", "to", "ba",
       "tst"
    };

    public static final String [] procInstQueryParams = {
        "processinstancestatus",
        "processversion",
        "startdate", "enddate"
    };

    public static final String [] procInstQueryParamsShort = {
        "pist",
        "pv",
        "stdt", "edt"
    };

    public static final String [] varInstQueryParams = {
       "varid", "varvalue",
       "var", "varregex",
       "all"
    };

    public static final String [] varInstQueryParamsShort = {
       "vid", "vv",
       null, "vr",
       null
    };

    static final String [] nameValueParams = {
        varInstQueryParams[2], // "var"
        varInstQueryParams[3], varInstQueryParamsShort[3], // "varregex"
    };

    static final String [] minMaxParams = {
        generalQueryParams[0], generalQueryParamsShort[0], // process instance id
        taskQueryParams[0], taskQueryParamsShort[0], // task id
        procInstQueryParams[2], procInstQueryParamsShort[2], // start date
        procInstQueryParams[3], procInstQueryParamsShort[3], // start date
    };

    static final String [] regexParams = {
        generalQueryParams[1], generalQueryParamsShort[1], // process id
        generalQueryParams[2], generalQueryParamsShort[2], // deployment id
        taskQueryParams[1], taskQueryParamsShort[1], // initiator
        taskQueryParams[2], taskQueryParamsShort[2], // stakeholder
        taskQueryParams[3], taskQueryParamsShort[3], // potential owner
        taskQueryParams[4], taskQueryParamsShort[4], // task owner
        taskQueryParams[5], taskQueryParamsShort[5], // business administrator
        procInstQueryParams[1], procInstQueryParamsShort[1], // process version
        varInstQueryParams[0], varInstQueryParamsShort[0], // variable id
        varInstQueryParams[1], varInstQueryParamsShort[1], // variable value
        varInstQueryParams[3], varInstQueryParamsShort[3], // variable value
    };

    public static final String [] metaRuntimeParams = {
       "memory", "history"
    };

    public static final String [] metaRuntimeParamsShort = {
       "mem", "hist"
    };
    // @formatter:on

    public static final Map<Integer, String> actionParamNameMap = new ConcurrentHashMap<Integer, String>();
    public static final Map<String, Integer> paramNameActionMap = new ConcurrentHashMap<String, Integer>();

    protected static final int GENERAL_END = 3;
    protected static final int TASK_END = 10;
    protected static final int PROCESS_END = 14;
    protected static final int VARIABLE_END = 19;
    protected static final int META_END = 21;

    static {
        // there are faster ways to do this, but the checks here are very valuable
        int idGen = 0;
        idGen = addParamsToActionMap(idGen, generalQueryParams, generalQueryParamsShort);
        if( idGen != GENERAL_END ) {
            throw new KieRemoteServicesInternalError("General query parameters [" + idGen + "]");
        }
        idGen = addParamsToActionMap(idGen, taskQueryParams, taskQueryParamsShort);
        if( idGen != TASK_END ) {
            throw new KieRemoteServicesInternalError("Task query parameters [" + idGen + "]" );
        }
        idGen = addParamsToActionMap(idGen, procInstQueryParams, procInstQueryParamsShort);
        if( idGen != PROCESS_END ) {
            throw new KieRemoteServicesInternalError("Process instance query parameters [" + idGen + "]" );
        }
        idGen = addParamsToActionMap(idGen, varInstQueryParams, varInstQueryParamsShort);
        if( idGen != VARIABLE_END ) {
            throw new KieRemoteServicesInternalError("Variable instance query parameters [" + idGen + "]" );
        }
        idGen = addParamsToActionMap(idGen, metaRuntimeParams, metaRuntimeParamsShort);
        if( idGen != META_END ) {
            throw new KieRemoteServicesInternalError("Meta parameters [" + idGen + "]" );
        }
    }

    private static int addParamsToActionMap(int idGen, String [] params, String [] paramsShort ) {
        if( params.length != paramsShort.length ) {
            throw new KieRemoteServicesInternalError( params.length + " params but " + paramsShort.length + " abbreviated params!");
        }
        for( int i = 0; i < params.length; ++i ) {
            int id = idGen++;
            paramNameActionMap.put(params[i], id);
            actionParamNameMap.put(id, params[i]);
            if( paramsShort[i] != null ) {
                paramNameActionMap.put(paramsShort[i], id);
            }
        }
        return idGen;
    }

    private static final Set<String> allQueryParameters = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    private static final Set<String> procInstOnlyQueryParameters = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    static {
        Set<String> taskQueryParamSet = new HashSet<String>();
        {
            String [][] taskQueryParamArrs = {
                    taskQueryParams, taskQueryParamsShort,
            };
            for( String [] paramArr : taskQueryParamArrs ) {
                for( String param : paramArr ) {
                    if( param != null ) {
                        allQueryParameters.add(param);
                        taskQueryParamSet.add(param);
                    }
                }
            }
        }
        {
            String [][] procInstOnlyQueryParamArrs = {
                    generalQueryParams, generalQueryParamsShort,
                    procInstQueryParams, procInstQueryParamsShort,
                    varInstQueryParams, varInstQueryParamsShort
                    // metaRuntimeParams, metaRuntimeParamsShort
            };
            for( String [] paramArr : procInstOnlyQueryParamArrs ) {
                for( String param : paramArr ) {
                    if( param != null ) {
                        procInstOnlyQueryParameters.add(param);
                        allQueryParameters.add(param);
                    }
                }
            }
        }

        for( String param : minMaxParams ) {
            String min = param + "_min";
            String max = param + "_max";
            allQueryParameters.add(min);
            allQueryParameters.add(max);
            if( ! taskQueryParamSet.contains(param) ) {
                procInstOnlyQueryParameters.add(min);
                procInstOnlyQueryParameters.add(max);
            }
        }
        for( String param : regexParams ) {
            param = param + "_re";
            allQueryParameters.add(param);
            if( ! taskQueryParamSet.contains(param) ) {
                procInstOnlyQueryParameters.add(param);
            }
        }
    }

    public static Set<String> getQueryParameters(boolean getAllQueryParameters) {
       return getAllQueryParameters ? allQueryParameters : procInstOnlyQueryParameters;
    }

    // "get" parser methods -------------------------------------------------------------------------------------------------------

    public static long [] getLongs(int action, String [] data) {
        String name = actionParamNameMap.get(action);
        long [] result = new long[data.length];
        int i = 0;
        try {
           for( ; i < data.length; ++i) {
              result[i] = Long.valueOf(data[i]);
           }
        } catch( NumberFormatException nfe ) {
            throw KieRemoteRestOperationException.badRequest("Values for query parameter '" + name + "' must be long (" + data[i] + ")");
        }
        return result;
    }

    public static int [] getInts(int action, String [] data) {
        String name = actionParamNameMap.get(action);
        int [] result = new int[data.length];
        int i = 0;
        try {
           for( ; i < data.length; ++i) {
              result[i] = Integer.valueOf(data[i]);
           }
        } catch( NumberFormatException nfe ) {
            throw KieRemoteRestOperationException.badRequest("Values for query parameter '" + name + "' must be integers (" + data[i] + ")");
        }
        return result;
    }

    public static final SimpleDateFormat QUERY_PARAM_DATE_FORMAT
        = new SimpleDateFormat("yy-MM-dd_HH:mm:ss.SSS");

    public static Timestamp [] getDates(int action, String [] data) {
        Timestamp [] result = new Timestamp[data.length];
        for( int i = 0; i < data.length; ++i) {
            result[i] = parseDate(data[i]);
        }
        return result;
    }

    public static Timestamp parseDate(String dateStr) {
        String [] parts = dateStr.split("_");
        String [] dateParts = null;
        String [] timeParts = null;
        String parseDateStr = null;
        if( parts.length == 2 ) {
            dateParts = parts[0].split("-");
            if( dateParts.length != 3 ) {
                badDateString(dateStr);
            }
            timeParts = parts[1].split(":");
            if( timeParts.length != 3 ) {
                badDateString(dateStr);
            }
            parseDateStr = dateStr;
        } else if( parts.length == 1 ) {
            dateParts = parts[0].split("-");
            timeParts = parts[0].split(":");
            if( timeParts.length == 3 && dateParts.length == 1 ) {
                dateParts = QUERY_PARAM_DATE_FORMAT.format(new Date()).split("_")[0].split("-");
            } else if( dateParts.length == 3 && timeParts.length == 1 ) {
               String [] newTimeParts = { "0", "0", "0.000" };
               timeParts = newTimeParts;
            } else {
                badDateString(dateStr);
            }
        } else {
            badDateString(dateStr);
        }
        // backwards compatibility for when it was just "yy-MM-dd_HH:mm:ss"
        if( parseDateStr == null ) {
            StringBuilder tmpStr = new StringBuilder(dateParts[0]);
            for( int i = 1; i < 3; ++i ) {
               tmpStr.append("-").append(dateParts[i]);
            }
            tmpStr.append("_");
            tmpStr.append(timeParts[0]);
            if( ! timeParts[2].contains(".") ) {
               timeParts[2] += ".000";
            }
            for( int i = 1; i < 3; ++i ) {
               tmpStr.append(":").append(timeParts[i]);
            }
            parseDateStr = tmpStr.toString();
        } else if( ! timeParts[2].contains(".") ) {
           parseDateStr += ".000";
        }

        try {
            Date date = QUERY_PARAM_DATE_FORMAT.parse(parseDateStr);
            return new Timestamp(date.getTime());
        } catch( ParseException pe ) {
            badDateString(parseDateStr);
            // this should never be thrown
            throw new KieRemoteServicesInternalError("Unable to parse date string '"  + parseDateStr + "'", pe);
        }
    }

    private static void badDateString(String dateStr) {
        throw KieRemoteRestOperationException.badRequest("'" + dateStr + "' is not a valid format for a date value query parameter.");
    }

    public static Status [] getTaskStatuses(String [] data) {
       Status [] result =  new Status[data.length];
       for( int i = 0; i < data.length; ++i ) {
          if( data[i].toLowerCase().equals(Status.InProgress.toString().toLowerCase()) ) {
             result[i] = Status.InProgress;
          } else {
              String value = data[i].substring(0, 1).toUpperCase() + data[i].substring(1);
              try {
                 result[i] = Status.valueOf(value);
              } catch( Exception e ) {
                 throw KieRemoteRestOperationException.badRequest("Task status '" + data[i] + "' is not valid.");
              }
          }
       }
       return result;
    }

    public static boolean isNameValueParam(String queryParam) {
        for( String allowedParam : nameValueParams ) {
            if( queryParam.toLowerCase().startsWith(allowedParam + "_")) {
                return true;
            }
        }
        return false;
    }

}
