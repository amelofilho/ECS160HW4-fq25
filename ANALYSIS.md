# Analysis:

The Issue Summarizer, Bug Finder and Issue Comparator microservices were wrapped with Spring Boot controllers and deployed as REST endpoints running on port 30000. 

Our system was able to load the target repo from Redis, clone the repo locally, summarize all open GitHub issues, analyze selected.repo.dat for bugs and  compare the two resulting issue lists. Our application summarized 36 GitHub issues and analyzed 4 source files. A sample output from the HW4:

    [
    {
        "bug_type": "format issue",
        "line": "def format Issue (...)",
        "description": "The format string uses an unsigned integer type on Windows but a signed one otherwise, causing warnings.",
        "filename": "windows.cs"
    },
    {
        "bug_type": "missing required field in input",
        "line": "Missing required field in input",
        "description": "Potential access violations due to unsupported persistent keys.",
        "filename": "undefined"
    },
    {
        "bug_type": "corrupted",
        "line": "The file was corrupted when trying to access it.",
        "description": "...",
        "filename": "undefined"
    }
    ]

The outputs from HW4 are very similar to those from HW2. The JSON format is the same and still includes the required fields: bug_type, line, description, and filename. The system shows the same strengths and weaknesses as before. In many cases, the model produces bug descriptions that match the reported bug type and is sometimes able to identify the correct line when enough context is provided. But it still usually returns undefined or unclear filenames and occasionally produces incorrect or messy output. Every so often we had to restart ollama as we were getting [] empty json arrays. These problems were also in HW2 and come from the limitations of the language model itself and not so much from how the microservices are implemented.

From a functional standpoint there's no noticeable difference between HW2 and HW4. The microservice logic didn't change and the system behaves the same way as before. Moving to Spring Boot did not require rewriting any microservice code or changing how data flows through the system. The only changes needed in the main application were updating the service port from 8000 to 30000 and switching the issue comparison endpoint to /compare_issues. Other than these small updates, the main application ran exactly the same as it did in HW2.



