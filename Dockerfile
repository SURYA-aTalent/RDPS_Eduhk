FROM ubuntu:22.04

# Set environment variables for Oracle Instant Client
ENV ORACLE_HOME=/opt/oracle/instantclient_23_2
ENV LD_LIBRARY_PATH=$ORACLE_HOME
ENV PATH=$ORACLE_HOME:$PATH

# Install dependencies
RUN apt-get update && apt-get install -y unzip libaio1

# Create Oracle directory
RUN mkdir -p $ORACLE_HOME

# Download and unzip Oracle Instant Client Basic and SQL*Plus
RUN apt-get install -y wget && \
    wget https://download.oracle.com/otn_software/linux/instantclient/2320000/instantclient-basic-linux.x64-23.2.0.0.0dbru.zip && \
    wget https://download.oracle.com/otn_software/linux/instantclient/2320000/instantclient-sqlplus-linux.x64-23.2.0.0.0dbru.zip && \
    unzip instantclient-basic-linux.x64-23.2.0.0.0dbru.zip -d $ORACLE_HOME && \
    unzip instantclient-sqlplus-linux.x64-23.2.0.0.0dbru.zip -d $ORACLE_HOME && \
    rm instantclient-basic-linux.x64-23.2.0.0.0dbru.zip instantclient-sqlplus-linux.x64-23.2.0.0.0dbru.zip

# Add a symbolic link for sqlplus
RUN ln -s $ORACLE_HOME/sqlplus /usr/bin/sqlplus

WORKDIR /work

CMD ["/bin/bash"]
