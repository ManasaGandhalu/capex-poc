#!/bin/sh
api_url="https://api.cf.us10-001.hana.ondemand.com"
organization="d3886a8ftrial_capex-trial-account-k217gga9"
space="CapEx Dev"
project_path="./"
mtar_file="mta_archives/CapEx_1.0.0-SNAPSHOT.mtar"
mtaext="dev.mtaext"

#Login to CF space
echo "\nCloud Foundry: Authenticate"
cf oauth-token
if [ $? -eq 0 ]; then
    echo "\nCloud Foundry: Authenticated."
    cf target -o "${organization}" -s "${space}"
else
    echo "\nCloud Foundry: Authenticating..."
    cf login -a "${api_url}" --sso -o "${organization}" -s "${space}"
fi
if [ $? -ne 0 ]; then
    echo "\nCloud Foundry: Unable to Authenticate. Check org/space role."
    exit
fi

echo "\nBuilding: Project"
mbt build -s "${project_path}"

if [ $? -eq 0 ]; then
    echo "\nDeploying: ${space}"
    cf deploy "${mtar_file}" -f
    cds deploy --to hana
else
    echo "Error: invalid mtar file"
fi