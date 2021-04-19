# Monzo Crawler
Crawler for Monzo take home assignment. It crawls all URLs within a single domain and creates an HTML file with the result. The program is written in Java 11 and uses Maven

# Create JAR file
- Run `mvn clean compile assembly:single`

# Run JAR file
- Run generated file in the target folder called web.crawler-0.0.1-SNAPSHOT-jar-with-dependencies with the command ` java -jar web.crawler-0.0.1-SNAPSHOT-jar-with-dependencies.jar {arg1} {arg2} {arg3} {arg4}` passing the 4 mandatory arguments
- A file called `result.html` will be generated with a table containing all the URLs crawled along with their links

# Arguments
4 arguments are needed when running the jar file
- {arg1} URL with the format https://monzo.com or https://www.monzo.com
- {arg2} Number of threads to use. Must be greater than 0
- {arg3} Boolean indicating if the program should look for the robots.txt and don't crawl the disallowed pages. Accepts true or false
- {arg4} Boolean indicating if the program should output to the console a line for each URL parsed along with the number of links on it. Accepts true or false

# Example
`java -jar web.crawler-0.0.1-SNAPSHOT-jar-with-dependencies.jar https://monzo.com 20 true false`
This command will crawl the website https://monzo.com with 20 threads, it will not crawl pages disallowed in the robots.txt file, and will only output errors to the console
