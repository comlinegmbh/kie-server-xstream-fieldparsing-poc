package de.comline.kie;
/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/


import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.commands.CallContainerCommand;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.CreateContainerCommand;
import org.kie.server.api.commands.DisposeContainerCommand;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponsesList;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.basetests.KieServerBaseIntegrationTest;

public class Poc extends KieServerBaseIntegrationTest {
    private static ReleaseId releaseId = new ReleaseId("foo.bar", "baz", "2.1.0.GA");

    private static final String CONTAINER_ID = "kie1";


    @BeforeClass
    public static void initialize() throws Exception {
        KieServerDeployer.createAndDeployKJar(releaseId);


        createContainer(CONTAINER_ID, releaseId);
    }


    @Test
    public void testProperFieldsScript() throws Exception {

        String payload = "<batch-execution lookup=\"defaultKieSession\">\n"
	        		+ "  <insert out-identifier=\"message\" return-object=\"true\" entry-point=\"DEFAULT\">\n"
	        		+ "    <org.pkg1.Message>\n"
	        		+ "      <text>HelloWorld</text>\n"
	        		+ "    </org.pkg1.Message>\n"
	        		+ "  </insert>\n"
	        		+ "  <fire-all-rules/>\n"
	        		+ "</batch-execution>";

        runBatchCommand(payload);
    }


    @Test
    public void testUnknownFieldScript() throws Exception {

        String payload = "<batch-execution lookup=\"defaultKieSession\">\n"
	        		+ "  <insert out-identifier=\"message\" return-object=\"true\" entry-point=\"DEFAULT\">\n"
	        		+ "    <org.pkg1.Message>\n"
	        		+ "      <text>HelloWorld</text>\n"
	        		+ "      <unknownField>this is new</unknownField>\n"
	        		+ "    </org.pkg1.Message>\n"
	        		+ "  </insert>\n"
	        		+ "  <fire-all-rules/>\n"
	        		+ "</batch-execution>";

        runBatchCommand(payload);
    }
    
    
	private void runBatchCommand(String payload) {
		String containerId = "command-script-container";
        KieServerCommand create = new CreateContainerCommand(new KieContainerResource( containerId, releaseId, null));
        KieServerCommand call = new CallContainerCommand(containerId, payload);
        KieServerCommand dispose = new DisposeContainerCommand(containerId);

        List<KieServerCommand> cmds = Arrays.asList(create, call, dispose);
        CommandScript script = new CommandScript(cmds);
        ServiceResponsesList reply = client.executeScript(script);

        for (ServiceResponse<? extends Object> r : reply.getResponses()) {
            Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, r.getType());
        }
	}
	
	@Override
    protected KieServicesClient createDefaultClient() throws Exception {
        KieServicesConfiguration configuration = KieServicesFactory.newRestConfiguration(TestConfig.getKieServerHttpUrl(), null, null);
        return createDefaultClient(configuration, MarshallingFormat.XSTREAM);
    }
}
