#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${YELLOW}Starting SmartHomes Data Setup...${NC}"

# Check if OPENAI_API_KEY is set
if [ -z "$OPENAI_API_KEY" ]; then
    echo -e "${YELLOW}Please enter your OpenAI API key:${NC}"
    read api_key
    export OPENAI_API_KEY=$api_key
fi

# Get project root directory
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
JAR_PATH="$PROJECT_ROOT/target/smarthomes-backend-jar-with-dependencies.jar"

# Verify Elasticsearch is running
echo -e "${YELLOW}Verifying Elasticsearch connection...${NC}"
if ! curl -s "http://localhost:9200" > /dev/null; then
    echo -e "${RED}Error: Elasticsearch is not running. Please start Elasticsearch first.${NC}"
    exit 1
fi

# Compile the project
echo -e "${YELLOW}Compiling project...${NC}"
cd "$PROJECT_ROOT"
mvn clean package -DskipTests

if [ ! -f "$JAR_PATH" ]; then
    echo -e "${RED}Error: JAR file not found at $JAR_PATH${NC}"
    echo -e "${YELLOW}Available files in target directory:${NC}"
    ls -l "$PROJECT_ROOT/target"
    exit 1
fi

# Run complete setup
echo -e "${YELLOW}Running data setup...${NC}"
java -jar "$JAR_PATH" com.smarthomes.util.DataSetup

echo -e "${GREEN}Setup completed successfully!${NC}"



#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${YELLOW}Starting SmartHomes Data Setup...${NC}"

# OpenAI API Key setup
validate_api_key() {
    local key=$1
    if [[ ! $key =~ ^sk-[A-Za-z0-9-]{32,96}$ ]]; then
        return 1
    fi
    return 0
}

get_api_key() {
    while true; do
        echo -e "${YELLOW}Please enter your OpenAI API key (starts with 'sk-'):${NC}"
        read -r api_key
        if [[ $api_key =~ ^sk- ]]; then
            echo -e "${GREEN}API key format appears valid${NC}"
            export OPENAI_API_KEY=$api_key
            break
        else
            echo -e "${RED}Invalid API key format. It should start with 'sk-'${NC}"
            echo -e "${YELLOW}Would you like to try again? (y/n)${NC}"
            read -r retry
            if [[ $retry != "y" ]]; then
                echo -e "${RED}Setup cancelled.${NC}"
                exit 1
            fi
        fi
    done
}

# Check if OPENAI_API_KEY is set and valid
if [ -z "$OPENAI_API_KEY" ]; then
    get_api_key
else
    if [[ ! $OPENAI_API_KEY =~ ^sk- ]]; then
        echo -e "${RED}Existing OPENAI_API_KEY is invalid.${NC}"
        get_api_key
    fi
fi

# Get project root directory
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
JAR_PATH="$PROJECT_ROOT/target/smarthomes-backend-jar-with-dependencies.jar"

# Verify Elasticsearch is running
echo -e "${YELLOW}Verifying Elasticsearch connection...${NC}"
if ! curl -s "http://localhost:9200" > /dev/null; then
    echo -e "${RED}Error: Elasticsearch is not running. Please start Elasticsearch first.${NC}"
    exit 1
fi

# Compile the project
echo -e "${YELLOW}Compiling project...${NC}"
cd "$PROJECT_ROOT"
mvn clean package -DskipTests

if [ ! -f "$JAR_PATH" ]; then
    echo -e "${RED}Error: JAR file not found at $JAR_PATH${NC}"
    echo -e "${YELLOW}Available files in target directory:${NC}"
    ls -l "$PROJECT_ROOT/target"
    exit 1
fi

# Run complete setup
echo -e "${YELLOW}Running data setup...${NC}"
java -jar "$JAR_PATH" com.smarthomes.util.DataSetup

echo -e "${GREEN}Setup completed successfully!${NC}"


