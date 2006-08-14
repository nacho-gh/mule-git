#! /bin/sh
# There is no need to call this if you set the MULE_HOME in your environment
if [ -z "$MULE_HOME" ] ; then
  MULE_HOME=../../..
  export MULE_HOME
fi

# Set your application specific classpath like this
CLASSPATH=$MULE_HOME/samples/errorhandler/conf:$MULE_HOME/samples/errorhandler/classes
export CLASSPATH

exec $MULE_HOME/bin/mule -config eh-mule-config.xml

CLASSPATH=
export CLASSPATH
