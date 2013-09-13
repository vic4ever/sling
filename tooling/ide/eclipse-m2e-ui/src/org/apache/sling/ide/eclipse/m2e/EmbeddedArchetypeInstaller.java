/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sling.ide.eclipse.m2e;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.maven.DefaultMaven;
import org.apache.maven.Maven;
import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.installer.ArtifactInstaller;
import org.apache.maven.artifact.installer.DefaultArtifactInstaller;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.DefaultMavenExecutionResult;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.LegacySupport;
import org.codehaus.plexus.PlexusContainer;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.internal.MavenPluginActivator;
import org.eclipse.m2e.core.internal.embedder.MavenImpl;
import org.sonatype.aether.RepositorySystemSession;

@SuppressWarnings("restriction")
public class EmbeddedArchetypeInstaller {

	private final String groupId;
	private final String artifactId;
	private final String version;

	private final Map<String,InputStream> origins = new HashMap<String,InputStream>();
	
	public EmbeddedArchetypeInstaller(final String groupId,
			final String artifactId,
			final String version) {
		if (groupId==null || groupId.length()==0) {
			throw new IllegalArgumentException("groupId must not be empty");
		}
		if (artifactId==null || artifactId.length()==0) {
			throw new IllegalArgumentException("artifactId must not be empty");
		}
		if (version==null || version.length()==0) {
			throw new IllegalArgumentException("version must not be empty");
		}
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
	}
	
	public void addResource(String fileExtension, URL origin) throws IOException {
		origins.put(fileExtension, origin.openStream());
	}
	
	public void addResource(String fileExtension, File origin) throws FileNotFoundException {
		origins.put(fileExtension, new FileInputStream(origin));
	}
	
	public void installArchetype() {
		IMaven maven = MavenPlugin.getMaven();
		try{
			// first get the plexus container
		    PlexusContainer container = ((MavenImpl) MavenPlugin.getMaven()).getPlexusContainer();
		    
		    // then get the DefaultMaven
		    DefaultMaven mvn = (DefaultMaven) container.lookup(Maven.class);

		    // now create a RepositorySystemSession
		    MavenExecutionRequest request = new DefaultMavenExecutionRequest();
		    request.setLocalRepository(maven.getLocalRepository());
		    RepositorySystemSession repositorySession = mvn.newRepositorySession(request);
		    
		    // set the MavenSession on the LegacySupport
	        MavenExecutionResult result = new DefaultMavenExecutionResult();
			MavenSession session = new MavenSession( container, repositorySession, request, result );
			LegacySupport legacy = container.lookup(LegacySupport.class);
			legacy.setSession(session);
		    
			// then lookup the DefaultArtifactInstaller
		    DefaultArtifactInstaller dai = (DefaultArtifactInstaller) container.lookup(ArtifactInstaller.class);

		    final Set<Entry<String, InputStream>> entries = origins.entrySet();
		    for (Iterator<Entry<String, InputStream>> it = entries.iterator(); it.hasNext();) {
				final Entry<String, InputStream> entry = it.next();
				final String fileExtension = entry.getKey();
				final InputStream in = entry.getValue();
				File tmpFile = File.createTempFile("slingClipseTmp", fileExtension);
				FileOutputStream fos = new FileOutputStream(tmpFile);
				copyStream(in, fos);
				fos.close();
				in.close();
				Artifact jarArtifact = new DefaultArtifact(
						groupId, 
						artifactId, 
						version,
						"", fileExtension, "", 
						new DefaultArtifactHandler());
				dai.install(tmpFile, jarArtifact, maven.getLocalRepository());
				tmpFile.delete();
			}

			Archetype archetype = new Archetype();
			archetype.setGroupId(groupId);			            
			archetype.setArtifactId(artifactId);
			archetype.setVersion(version);
			org.apache.maven.archetype.Archetype archetyper = MavenPluginActivator.getDefault().getArchetype();
			archetyper.updateLocalCatalog(archetype);

//			ArchetypeCatalog defaultLocalCatalog = archetyper.getDefaultLocalCatalog();
//			defaultLocalCatalog.addArchetype(archetype);
//			manager.readCatalogs();
		} catch(Exception e) {
			e.printStackTrace();
		}

//		try {
//			ArtifactRepository localRepo = maven.getLocalRepository();
//			dai.install(file, artifact, localRepo);
//		} catch (ArtifactInstallationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (CoreException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		RepositorySystem ni = MavenPluginActivator.getDefault().getRepositorySystem();
//		ni.install(arg0, arg1);
//		manager.addArchetypeCatalogFactory(new ArchetypeCatalogFactory(id, description, editable) {
//
//			@Override
//			public ArchetypeCatalog getArchetypeCatalog()
//					throws CoreException {
//				ArchetypeCatalog cat = new ArchetypeCatalog();
//				Archetype myArchetype = new Archetype();
//				myArchetype.
//				cat.addArchetype(myArchetype);
//				return cat;
//			}
//	    	
//	    });

	}

	private void copyStream(InputStream in, OutputStream os) throws IOException {
		final byte[] bytes = new byte[4*1024];
		while (true) {
			final int numRead = in.read(bytes);
			if (numRead < 0) {
				break;
			}
			os.write(bytes, 0, numRead);
		}
	}
}