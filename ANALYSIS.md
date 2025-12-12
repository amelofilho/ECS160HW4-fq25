# Analysis:

Our Issue Summarizer, Bug Finder and Issue Comparator microservices were wrapped with Spring Boot controllers and deployed as REST endpoints running on port 30000. 

Our system was able to load the target repo from Redis, clone the repo locally, summarize all open GitHub issues, analyze selected.repo.dat for bugs and  compare the two resulting issue lists. Our application summarized 36 GitHub issues and analyzed 4 source files. We ran into a few cases: 

## Case 1:

    Summarized 36 Issues from Cloned Repo's Issues!
    Looked at 4 Files for Bugs!
    [
    {
        "bug_type": "Missing Required Parameters",
        "line": "undefined",
        "description": "The helper function schannel_v67 is missing a required parameter that should have been passed earlier in the flow.",
        "filename": "lib/pingpong.c"
    },
    {
        "bug_type": "Incorrect Initialization of certificate store",
        "line": "undefined",
        "description": "The certificate store initialization step did not complete properly, causing subsequent operations to fail.",
        "filename": "lib/http.c"
    }
    ]

The outputs from case 1 are very similar to those from HW2 and expected. The JSON format is the same and still includes the required fields: bug_type, line, description, and filename. The system shows the same strengths and weaknesses as before: Ollama produces bug descriptions that match the reported bug type and is sometimes able to identify the correct line when enough context is provided. But it still usually returns undefined or unclear filenames and occasionally produces incorrect or messy output.

## Case 2:

    []

This empty output means that the system was unable to find any matching or similar issues between the summarized GitHub issues and the bugs extracted from the source files. This behavior is the same as what we observed in HW2 and is expected given how inconsistent small language models can be. Again, ollama sometimes generates vague or unrelated descriptions which makes it difficult to reliably compare issues across the two lists.

## Case 3:

    [
    {"bug_type": "missing FMT-Smith", "line": "3847", "description": "FMT-Smith is not defined on line 2 of a C file at some log message...", "filename": "somefile.c"},
    {"bug_type": "unknown", "line": "undefined", "description": "", "filename": "undefined"}
    ]

In case 3, our system produced output that technically contained bug information but was poorly formatted or compressed. Even when explicitly told to return a structured JSON object, ollama sometimes ignores parts of the prompt or produces compressed or loosely formatted output.

## Conclusion:
The issues observed in the three cases are caused by limitations in the ollama’s ability to follow instructions rather than problems with the microservice design or the Spring Boot integration.

From a functional standpoint there's no noticeable difference between HW2 and HW4. The microservice logic didn't change and the system behaves the same way as before. Moving to Spring Boot didn't require rewriting any microservice code or changing how data flows through the system. The only changes needed in main-app were updating the service port from 8000 to 30000 and switching the issue comparison endpoint to /compare_issues. Other than these small updates main-app ran exactly the same as it did in HW2.