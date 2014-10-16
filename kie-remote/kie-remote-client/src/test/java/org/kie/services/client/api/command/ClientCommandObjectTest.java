package org.kie.services.client.api.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.services.task.impl.model.TaskImpl;
import org.junit.Test;
import org.kie.api.command.Command;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Task;
import org.kie.remote.jaxb.gen.ActivateTaskCommand;
import org.kie.remote.jaxb.gen.ClaimNextAvailableTaskCommand;
import org.kie.remote.jaxb.gen.GetTaskAssignedAsBusinessAdminCommand;
import org.kie.remote.jaxb.gen.GetTaskAssignedAsPotentialOwnerCommand;
import org.kie.remote.jaxb.gen.GetTasksByStatusByProcessInstanceIdCommand;
import org.kie.remote.jaxb.gen.GetTasksOwnedCommand;
import org.kie.remote.jaxb.gen.JaxbStringObjectPairArray;
import org.kie.remote.jaxb.gen.QueryFilter;
import org.kie.remote.jaxb.gen.TaskCommand;
import org.mockito.ArgumentCaptor;

public class ClientCommandObjectTest {

    @Test
    public void taskServiceClientTest() throws Exception { 
      
        RemoteConfiguration config = new RemoteConfiguration(RemoteConfiguration.Type.CONSTRUCTOR);
        TaskServiceClientCommandObject taskServiceClient = new TaskServiceClientCommandObject(config);
        TaskServiceClientCommandObject taskServiceClientSpy = spy(taskServiceClient);
        ArgumentCaptor<Command> cmdCaptor = ArgumentCaptor.forClass(Command.class);
        doReturn(new Object()).when(taskServiceClientSpy).executeCommand(cmdCaptor.capture());
        
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("data", "value");
        dataMap = Collections.unmodifiableMap(dataMap);
        List<Object> list = new ArrayList<Object>();
        list.add("elem");
        Task task  = new TaskImpl();
        ActivateTaskCommand activateTaskCommand = new ActivateTaskCommand();
        activateTaskCommand.setTaskId(23l);
        activateTaskCommand.setUserId("illuminati");
        
        Map<String, Object[]> methodArgsMap = new LinkedHashMap<String, Object[]>();
        List<Method> taskServiceMethods = Arrays.asList(TaskService.class.getMethods());
        Collections.sort(taskServiceMethods, new Comparator<Method>() {
            @Override
            public int compare( Method o1, Method o2 ) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for( Method taskMethod : taskServiceMethods ) { 
           Class<?> [] paramTypes = taskMethod.getParameterTypes(); 
           Object [] params = new Object[paramTypes.length];
           for( int i = 0; i < paramTypes.length; ++i ) { 
               if( long.class.isAssignableFrom(paramTypes[i]) ) { 
                   params[i] = 23l;
               } else if( String.class.isAssignableFrom(paramTypes[i]) ) { 
                   params[i] = "user";
               } else if( Map.class.isAssignableFrom(paramTypes[i]) ) { 
                   params[i] = dataMap;
               } else if( List.class.isAssignableFrom(paramTypes[i]) ) { 
                   params[i] = list;
               } else if( Task.class.isAssignableFrom(paramTypes[i]) ) { 
                   params[i] = task;
               } else if( Command.class.isAssignableFrom(paramTypes[i]) ) { 
                   params[i] = activateTaskCommand;
                   // TODO: reactivate when execute is enabled
                   continue;
               } else { 
                   fail( taskMethod.getName() + ": param type " + paramTypes[i].getSimpleName() + " encountered!");
               }
           }
           
           String key = taskMethod.getName().toLowerCase();
           Object [] prevVal = methodArgsMap.put(key, params);
           assertNull( "Method " + key + " already tested!?!", prevVal);
           
           try { 
               taskMethod.invoke(taskServiceClientSpy, params);
           } catch( Throwable t) { 
               // ignore this, we're just testing the method -> command mappings
           }
        } 
        
        List<Command> capturedCommands = cmdCaptor.getAllValues();
        for( Command methodCmd : capturedCommands ) { 
            String cmdName = methodCmd.getClass().getSimpleName();
            int last = cmdName.indexOf("Command");
            int lastTask = cmdName.indexOf("TaskCommand");
            String cmdMethodName = cmdName.substring(
                    0, 
                    lastTask > 0 ? lastTask : last )
                    .toLowerCase();
            if( cmdMethodName.equals("add") ) { 
                cmdMethodName = "addtask";
            }
            Object [] params = methodArgsMap.get(cmdMethodName);
            if( params == null ) { 
                if( cmdMethodName.length() < 4 ) { 
                    cmdMethodName += "task";
                } else if( cmdMethodName.length() < 20 ) {
                   cmdMethodName += "byid"; 
                }
                if( "gettask".equals(cmdMethodName) ) { 
                   cmdMethodName += "byid"; 
                }
                if( "gettaskassignedasbusinessadmin".equals(cmdMethodName) ) { 
                    cmdMethodName = "gettasksassignedasbusinessadministrator";
                }
                if( "gettaskassignedaspotentialowner".equals(cmdMethodName) ) { 
                    cmdMethodName = "gettasksassignedaspotentialowner";
                }
                params = methodArgsMap.get(cmdMethodName);
            }
            if( methodCmd instanceof GetTaskAssignedAsPotentialOwnerCommand
                    || methodCmd instanceof GetTasksOwnedCommand ) { 
                Field statusListField = methodCmd.getClass().getDeclaredField("statuses");
                statusListField.setAccessible(true);
                Object statusList = statusListField.get(methodCmd);
                if( statusList != null ) { 
                   params = methodArgsMap.get(cmdMethodName + "bystatus");
                }
            }
            assertNotNull(cmdMethodName + ": no params found!", params);
           
            int paramsChecked = 0;
            List<Field> cmdFields = new ArrayList<Field>(Arrays.asList(methodCmd.getClass().getDeclaredFields()));
            cmdFields.addAll(Arrays.asList(TaskCommand.class.getDeclaredFields()));
            for( Field cmdField : cmdFields ) { 
               cmdField.setAccessible(true);
               Object fieldVal = cmdField.get(methodCmd);
               if( fieldVal == null ) { 
                   continue;
               }
               if( fieldVal instanceof QueryFilter ) { 
                   fieldVal = ((QueryFilter) fieldVal).getLanguage();
               }
               boolean found = false; 
               for( Object val : params ) { 
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
                  }
               }
               assertTrue( methodCmd.getClass().getSimpleName() + "." + cmdField.getName() + " not filled!", found);
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
}
