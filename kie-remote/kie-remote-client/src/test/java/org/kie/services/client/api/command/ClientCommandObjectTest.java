/*
 * Copyright 2015 JBoss Inc
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

package org.kie.services.client.api.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.xml.datatype.XMLGregorianCalendar;

import org.jbpm.services.task.impl.model.GroupImpl;
import org.jbpm.services.task.impl.model.OrganizationalEntityImpl;
import org.jbpm.services.task.impl.model.TaskImpl;
import org.jbpm.services.task.impl.model.xml.JaxbComment;
import org.junit.Test;
import org.kie.api.command.Command;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Comment;
import org.kie.api.task.model.Task;
import org.kie.remote.jaxb.gen.ActivateTaskCommand;
import org.kie.remote.jaxb.gen.AddCommentCommand;
import org.kie.remote.jaxb.gen.ClaimNextAvailableTaskCommand;
import org.kie.remote.jaxb.gen.GetTaskAssignedAsBusinessAdminCommand;
import org.kie.remote.jaxb.gen.GetTasksByStatusByProcessInstanceIdCommand;
import org.kie.remote.jaxb.gen.JaxbStringObjectPairArray;
import org.kie.remote.jaxb.gen.OrganizationalEntity;
import org.kie.remote.jaxb.gen.QueryCriteria;
import org.kie.remote.jaxb.gen.QueryFilter;
import org.kie.remote.jaxb.gen.QueryWhere;
import org.kie.remote.jaxb.gen.SetTaskPropertyCommand;
import org.kie.remote.jaxb.gen.TaskCommand;
import org.mockito.ArgumentCaptor;

public class ClientCommandObjectTest {

    private static final Random random = new Random();
    
    /**
     * This test makes sure that the right command is called for the right method in the TaskService implementation
     * @throws Exception
     */
    @Test  
    public void taskServiceClientTest() throws Exception { 
     
        // mock setup
        RemoteConfiguration config = new RemoteConfiguration(RemoteConfiguration.Type.CONSTRUCTOR);
        TaskServiceClientCommandObject taskServiceClient = new TaskServiceClientCommandObject(config);
        TaskServiceClientCommandObject taskServiceClientSpy = spy(taskServiceClient);
        ArgumentCaptor<Command> cmdCaptor = ArgumentCaptor.forClass(Command.class);
        doReturn(new Object()).when(taskServiceClientSpy).executeCommand(cmdCaptor.capture());
      
        // setup of arguments
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("data", "value");
        dataMap = Collections.unmodifiableMap(dataMap);
        List<Object> list = new ArrayList<Object>();
        list.add("elem");
        Task task  = new TaskImpl();
        ActivateTaskCommand activateTaskCommand = new ActivateTaskCommand();
        activateTaskCommand.setTaskId(23l);
        activateTaskCommand.setUserId("illuminati");
       
        // sort methods to test consistently
        List<Method> taskServiceMethods = Arrays.asList(TaskService.class.getMethods());
        Collections.sort(taskServiceMethods, new Comparator<Method>() {
            @Override
            public int compare( Method o1, Method o2 ) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        
        // call each method in the TaskService
        for( Method taskMethod : taskServiceMethods ) { 
            if( taskMethod.getName().equals("execute") ) { 
                continue;
            }
           Class<?> [] paramTypes = taskMethod.getParameterTypes(); 
           Object [] params = new Object[paramTypes.length];
           if( taskMethod.getName().equals("getTasksByVariousFields")
                   && paramTypes[1].equals(Map.class) ) { 
               // not supported
               continue;
           }
           for( int i = 0; i < paramTypes.length; ++i ) { 
               if( long.class.isAssignableFrom(paramTypes[i]) ) { 
                   params[i] = random.nextLong();
               } else if( String.class.isAssignableFrom(paramTypes[i]) ) { 
                   params[i] = UUID.randomUUID().toString();
               } else if( Map.class.isAssignableFrom(paramTypes[i]) ) { 
                   params[i] = dataMap;
               } else if( List.class.isAssignableFrom(paramTypes[i]) ) { 
                   params[i] = list;
                   if( taskMethod.getName().equals("nominate") ) {
                      List<org.kie.api.task.model.OrganizationalEntity> orgEntList = new ArrayList<org.kie.api.task.model.OrganizationalEntity>();
                      OrganizationalEntityImpl orgEnt = new GroupImpl(UUID.randomUUID().toString());
                      orgEntList.add(orgEnt);
                      params[i] = orgEntList;
                   }
               } else if( Task.class.isAssignableFrom(paramTypes[i]) ) { 
                   params[i] = task;
               } else if( Command.class.isAssignableFrom(paramTypes[i]) ) { 
                   params[i] = activateTaskCommand;
                   // TODO: reactivate when execute is enabled
                   continue;
               } else if( int.class.isAssignableFrom(paramTypes[i]) ) { 
                   params[i] = random.nextInt(Integer.MAX_VALUE);
               } else if( boolean.class.isAssignableFrom(paramTypes[i]) ) { 
                   params[i] = false;
               } else if( Comment.class.isAssignableFrom(paramTypes[i]) ) { 
                   JaxbComment comment = new JaxbComment("user", new Date(), UUID.randomUUID().toString());
                   params[i] = comment;
               } else if( Date.class.isAssignableFrom(paramTypes[i]) ) { 
                   params[i] = new Date();
               } else { 
                   fail( taskMethod.getName() + ": param type " + paramTypes[i].getSimpleName() + " encountered!");
               }
           }
          
           // call the method
           try { 
               taskMethod.invoke(taskServiceClientSpy, params);
           } catch( Throwable t) { 
               // ignore this, we're just testing the method -> command mappings
           }
        
           // Look at all the commands we've tested
           Command methodCmd = cmdCaptor.getValue();
            int paramsChecked = 0;
            List<Field> cmdFields = new ArrayList<Field>(Arrays.asList(methodCmd.getClass().getDeclaredFields()));
            cmdFields.addAll(Arrays.asList(TaskCommand.class.getDeclaredFields()));
            FIELD_CHECK: for( Field cmdField : cmdFields ) { 
               cmdField.setAccessible(true);
               Object fieldVal = cmdField.get(methodCmd);
               if( fieldVal == null ) { 
                   continue;
               }
               if( fieldVal instanceof QueryFilter ) { 
                   QueryFilter filter = (QueryFilter) fieldVal; 
                   fieldVal = filter.getLanguage();
                   if( fieldVal != null ) { 
                      boolean langFound = matchFieldValue(fieldVal, params);
                      assertTrue( methodCmd.getClass().getSimpleName() + "." + cmdField.getName() + " [language] not filled!", langFound);
                      ++paramsChecked;
                   } 
                   
                   fieldVal = filter.getOffset();
                   if( fieldVal != null ) { 
                      boolean offsetFound = matchFieldValue(fieldVal, params);
                      assertTrue( methodCmd.getClass().getSimpleName() + "." + cmdField.getName() + " [offset] not filled!", offsetFound);
                      ++paramsChecked;
                   } 
                   
                   fieldVal = filter.getCount();
                   if( fieldVal != null && ((Integer) fieldVal) != -1 ) {
                      boolean countFound = matchFieldValue(fieldVal, params);
                      assertTrue( methodCmd.getClass().getSimpleName() + "." + cmdField.getName() + " [count] not filled!", countFound);
                      ++paramsChecked;
                   } 
                   continue;
               }
               if( methodCmd instanceof SetTaskPropertyCommand && fieldVal instanceof BigInteger ) { 
                 if( ((BigInteger) fieldVal).longValue() == 5 ) { 
                    for( Object param : params ) { 
                       if( param instanceof Date ) { 
                          ++paramsChecked;
                          continue FIELD_CHECK;
                       }
                    }
                 }
               }
               boolean found = matchFieldValue(fieldVal, params);

               assertTrue( methodCmd.getClass().getSimpleName() + "." + cmdField.getName() + " not filled!", found);
               ++paramsChecked;
            }
            if( methodCmd instanceof AddCommentCommand && params.length == 3 ) { 
                ++paramsChecked;
            }
            
            // WACKY!?!
            if( methodCmd instanceof ClaimNextAvailableTaskCommand ) { 
                // language param not used for this?
                continue;
            }
            if( methodCmd instanceof GetTaskAssignedAsBusinessAdminCommand 
                    || methodCmd instanceof GetTasksByStatusByProcessInstanceIdCommand ) { 
                // language param not used!
                continue;
            }
            
            assertEquals( "Too many null values in " + methodCmd.getClass().getSimpleName(), params.length, paramsChecked );
        }
    }

    private boolean matchFieldValue(Object fieldVal, Object [] inputValues ) { 
        boolean found = false;
        for( Object val : inputValues ) { 
            if( val.equals(fieldVal) ) { 
                found = true;
                break;
            } else if( fieldVal instanceof org.kie.remote.jaxb.gen.Task ) { 
                if( val instanceof Task ) { 
                    found = true;
                    break;
                }
            } else if( fieldVal instanceof JaxbStringObjectPairArray ) { 
                if( val instanceof Map ) { 
                    found = true;
                    break;
                }
            } else if( fieldVal instanceof org.kie.remote.jaxb.gen.Comment ) { 
                org.kie.remote.jaxb.gen.Comment fieldComment 
                    = (org.kie.remote.jaxb.gen.Comment) fieldVal;
                if( val instanceof JaxbComment ) { 
                   JaxbComment jaxbVal = (JaxbComment) val;
                   if( jaxbVal.getText().equals(fieldComment.getText()) ) { 
                       found = true;
                       break;
                   }
                } else if( val instanceof String ) { 
                    if( fieldComment.getAddedBy().equals(val) 
                            || fieldComment.getText().equals(val) ) { 
                        found = true;
                        break;
                    } 
                } 
            } else if( fieldVal instanceof XMLGregorianCalendar && val instanceof Date ) { 
                if( ((XMLGregorianCalendar) fieldVal).toGregorianCalendar().getTime().equals(val) ) { 
                   found = true;
                   break;
                }
            } else if( fieldVal instanceof List && val instanceof List ) { 
               for( Object elem : (List) fieldVal ) { 
                  if( elem instanceof OrganizationalEntity ) { 
                      OrganizationalEntity orgEnt = (OrganizationalEntity) elem;
                      if( ((List) val).contains(new GroupImpl(orgEnt.getId()))) { 
                         found = true; 
                         break;
                      }
                  }
               }
            } else if( fieldVal instanceof QueryWhere ) { 
               List<QueryCriteria> criteriaList = ((QueryWhere) fieldVal).getQueryCriterias();
               while( ! criteriaList.isEmpty() ) { 
                   List<QueryCriteria> moreCriterias = new ArrayList<QueryCriteria>();
                   Iterator<QueryCriteria> iter = criteriaList.iterator();
                   while( iter.hasNext() ) { 
                       QueryCriteria crit = iter.next();
                       iter.remove();
                       moreCriterias.addAll(crit.getCriterias());
                       if( crit.getParameters().contains(val)
                               || crit.getDateParameters().contains(val) ) { 
                           return true;
                       }
                   }
                   criteriaList.addAll(moreCriterias);
               }
            }
        } 
        return found;
    }
}
