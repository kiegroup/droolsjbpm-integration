# @author Michael Neale
# Was tested in rails 2.1

    require 'json'
    http = Net::HTTP.new('localhost', 8080)
    path = "/drools-server/knowledgebase/teamallocation"
    post_data = {"knowledgebase-request" => {                     
                  :globals => {"named-fact" => [{:id => "a", :fact => {"@class" => "teamallocation.Assignment"}}]},
                  :inFacts => {"anon-fact" => [{:fact => {"@class" => "teamallocation.Claim", "value" => 150}}]},
                  :inOutFacts => {"named-fact" => [{:id => "x", :fact => {"@class" => "teamallocation.Team", "specialty" => "FATAL"}},
                                                   {:id => "y", :fact => {"@class" => "teamallocation.Team"}}]}
                                            }                    
                 }
    headers = {
      "Content-Type" => "application/json"
    }
    resp, data = http.post(path, post_data.to_json, headers)
    
    
    answer = JSON.parse(data)
    #digging out the results:
    puts answer["knowledgebase-response"]["globals"]["named-fact"]["fact"]["teamName"]
    #if there is more then one fact, they are a list
    puts answer["knowledgebase-response"]["inOutFacts"]["named-fact"][0]["fact"]["specialty"]
