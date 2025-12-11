package com.ecs160;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.ecs160.microservices.llmservice;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;



public class AppTest 
{
    public Gson gson;
    @Before
    public void initalize(){
        gson =new Gson();
    }

    private void assertValidBugFormat(String jsonResponse,boolean array) {

        if(array){
            // convert to array
            JsonArray array_of_json=gson.fromJson(jsonResponse, JsonArray.class);
            int array_size=array_of_json.size();

            for(int i=0;i<array_size;i++){
                // get the element of array as JsonObject
                JsonObject obj = array_of_json.get(i).getAsJsonObject();

                 // check all field present
                assertTrue("'bug_type' Present", obj.has("bug_type"));
                assertTrue("'line' Present", obj.has("line"));
                assertTrue("'description' Present", obj.has("description"));
                assertTrue("'filename' Present", obj.has("filename"));
                //check all field non null
                assertNotNull(obj.get("bug_type").getAsString());
                assertNotNull(obj.get("line").getAsString());
                assertNotNull(obj.get("description").getAsString());
                assertNotNull(obj.get("filename").getAsString());
            }

        }
        else{
             JsonObject obj = gson.fromJson(jsonResponse, JsonObject.class);
            // check all field present
            assertTrue("'bug_type' Present", obj.has("bug_type"));
            assertTrue("'line' Present", obj.has("line"));
            assertTrue("'description' Present", obj.has("description"));
            assertTrue("'filename' Present", obj.has("filename"));
            //check all field non null
            assertNotNull(obj.get("bug_type").getAsString());
            assertNotNull(obj.get("line").getAsString());
            assertNotNull(obj.get("description").getAsString());
            assertNotNull(obj.get("filename").getAsString());

        }
        
       
    }

    @Test
    public void testSummarizeIssue() throws IOException{

        llmservice mockLlmservice=mock(llmservice.class);

        JsonObject testIssue = new JsonObject();
        testIssue.addProperty("Description", 
            "Application crashes with seg fault." +
            "The problem occured in file.c in line 145. " +
            "We will investigate further but a buffer overflow may caused the problem");

        String issueJson = gson.toJson(testIssue);

        String mockResponse = "{"
            + "\"bug_type\":\"BufferOverflow\","
            + "\"line\":\"145\","
            + "\"description\":\"Application crashes with segmentation fault, buffer overflow suspected\","
            + "\"filename\":\"file.c\""
            + "}";

        when(mockLlmservice.summarize_Issue(issueJson)).thenReturn(mockResponse);

        String summary = mockLlmservice.summarize_Issue(issueJson);

        // Ensure summary actually returns something
        assertNotNull(summary);
        // Ensure correct format for json return
        assertValidBugFormat(summary,false);

    }


    @Test
    public void testfindbugs() throws IOException{

        llmservice mockLlmservice=mock(llmservice.class);

        
         String cCode = "#include <stdio.h>\n" +
                "#include <strings.h>\n" +
                "#include <stdio.h>\n" +
                "#include <time.h>\n" +
                "#include <stdlib.h>\n" +
                "#include <ctype.h>\n" +
                "\n" +
                "\n" +
                "int num_of_words(char line){\n" +
                "    int numwords = 0;\n" +
                "    int inword = 0;\n" +
                "    while(line){\n" +
                "        if(isspace(line)){\n" +
                "            inword =0;\n" +
                "        }\n" +
                "        else if (inword ==0){\n" +
                "            inword = 1;\n" +
                "            numwords++;\n" +
                "        }\n" +
                "        line++;\n" +
                "    }\n" +
                "    return numwords;\n" +
                "}\n" +
                "int word_count(charfirst_word){\n" +
                "\n" +
                "}\n" +
                "\n" +
                "/*\n" +
                "char tokenize_words(char *line){\n" +
                "    word_count = num_of_words(line);\n" +
                "    char sentence =  malloc(sizeof(char)word_count);\n" +
                "    for(int i = word_count; i <=0; i--){\n" +
                "\n" +
                "    }\n" +
                "}*/\n" +
                "int main() {\n" +
                "\n" +
                "char line = malloc(sizeof(char)*80);\n" +
                "//char** words= NULL;\n" +
                "scanf(\"%[^\\n]s\", line);\n" +
                "printf(\"%s\", line);\n" +
                "printf(\"%d\", num_of_words(line));\n" +
                "//words = tokenize_words(line);\n" +
                "free(line);\n" +
                "free(line);\n" +
                "}";
        String filename = "test.c";

        String mockResponse = "{"
            + "\"bug_type\":\"DoubleFree\","
            + "\"line\":\"43\","
            + "\"description\":\"Memory freed twice on line 43 and 44\","
            + "\"filename\":\"test.c\""
            + "}";

        when(mockLlmservice.find_bugs(cCode,filename)).thenReturn(mockResponse);

        String bugsString = mockLlmservice.find_bugs(cCode,filename);

        // Ensure summary actually returns something
        assertNotNull(bugsString);
        // Ensure correct format for json return
        assertValidBugFormat(bugsString,false);

    }

    @Test
    public void testCompareIssues() throws IOException {
        llmservice mockLlmservice = mock(llmservice.class);

         String issueList1 = "[" +
            "{\"bug_type\":\"BufferOverflow\",\"line\":145,\"description\":\"Buffer overflow in file processing\",\"filename\":\"file.c\"}," +
            "{\"bug_type\":\"MemoryLeak\",\"line\":89,\"description\":\"Memory not freed\",\"filename\":\"memory.c\"}" +
            "]";
        
        String issueList2 = "[" +
            "{\"bug_type\":\"BufferOverflow\",\"line\":145,\"description\":\"Array index out of bounds when reading file header\",\"filename\":\"file.c\"}," +
            "{\"bug_type\":\"NullPointerException\",\"line\":23,\"description\":\"Null pointer dereference\",\"filename\":\"pointer.c\"}" +
            "]";

        String mockResponse = "[{" +
            "\"bug_type\":\"BufferOverflow\"," +
            "\"line\":\"145\"," +
            "\"description\":\"Buffer overflow in file processing\"," +
            "\"filename\":\"file.c\"" +
            "}]";

        when(mockLlmservice.IssueSummaryCompactor(issueList1,issueList2)).thenReturn(mockResponse);

        String response=mockLlmservice.IssueSummaryCompactor(issueList1,issueList2);
        
        // Ensure response actually returns something
        assertNotNull(response);
        // Ensure correct format for json return
        assertValidBugFormat(response,true);

    }



   
}
