/*
 * RSConnectDeployDialog.java
 *
 * Copyright (C) 2009-15 by RStudio, Inc.
 *
 * Unless you have received this program directly from RStudio pursuant
 * to the terms of a commercial license agreement with RStudio, then
 * this program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */
package org.rstudio.studio.client.rsconnect.ui;

import java.util.HashMap;
import java.util.Map;

import org.rstudio.core.client.files.FileSystemItem;
import org.rstudio.core.client.widget.ThemedButton;
import org.rstudio.studio.client.common.FilePathUtils;
import org.rstudio.studio.client.common.GlobalDisplay;
import org.rstudio.studio.client.rsconnect.RSConnect;
import org.rstudio.studio.client.rsconnect.model.RSConnectAccount;
import org.rstudio.studio.client.rsconnect.model.RSConnectDeploymentRecord;
import org.rstudio.studio.client.rsconnect.model.RSConnectPublishSource;
import org.rstudio.studio.client.rsconnect.model.RSConnectServerOperations;
import org.rstudio.studio.client.server.ServerError;
import org.rstudio.studio.client.server.ServerRequestCallback;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.CheckBox;

public class RSConnectDeployDialog 
             extends RSConnectDialog<RSConnectDeploy>
{
   public RSConnectDeployDialog(RSConnectServerOperations server, 
                                RSConnect connect,
                                final GlobalDisplay display, 
                                RSConnectPublishSource source,
                                RSConnectDeploymentRecord fromPrevious)
   {
      super(server, display, new RSConnectDeploy(source, fromPrevious, false));
      setText("Publish to Server");
      setWidth("350px");
      deployButton_ = new ThemedButton("Publish");
      addCancelButton();
      addOkButton(deployButton_);
      connect_ = connect;

      if (source.isDocument())
      {
         contents_.setNewAppName(
               FileSystemItem.createFile(source.getSourceFile()).getStem());
      }
      else
      {
         contents_.setNewAppName(
               FilePathUtils.friendlyFileName(source.getDeployDir()));
      }

      launchCheck_ = new CheckBox("Launch browser");
      launchCheck_.setValue(true);
      launchCheck_.setStyleName(contents_.getStyle().launchCheck());
      addLeftWidget(launchCheck_);
      
      contents_.setSourceDir(source.getDeployDir());
      
      deployButton_.addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            onDeploy();
         }
      });
      
      // don't enable the deploy button until we're done getting the file list
      deployButton_.setEnabled(false);
      
      // Get the deployments of this directory from any account (should be fast,
      // since this information is stored locally in the directory). 
      server_.getRSConnectDeployments(source.getDeployKey(), 
            new ServerRequestCallback<JsArray<RSConnectDeploymentRecord>>()
      {
         @Override
         public void onResponseReceived(
               JsArray<RSConnectDeploymentRecord> records)
         {
            processDeploymentRecords(records);
            if (records.length() == 1)
            {
               defaultAccount_ = records.get(0).getAccount();
               contents_.setDefaultAccount(defaultAccount_);
            }
         }

         @Override
         public void onError(ServerError error)
         {
            // If an error occurs we won't have any local deployment records,
            // but the user can still create new deployments.
         }
      });
      
      
      contents_.setOnDeployDisabled(new Command()
      {
         @Override
         public void execute()
         {
            deployButton_.setEnabled(false);
         }
      });

      contents_.setOnDeployEnabled(new Command()
      {
         @Override
         public void execute()
         {
            deployButton_.setEnabled(true);
         }
      });
      
      contents_.onActivate(addProgressIndicator());
   }
   
   private void onDeploy()
   {
      connect_.fireRSConnectPublishEvent(
            contents_.getResult(), launchCheck_.getValue());
      closeDialog();
   }
   
   // Create a lookup from app URL to deployments made of this directory
   // to that URL
   private void processDeploymentRecords(
         JsArray<RSConnectDeploymentRecord> records)
   {
      for (int i = 0; i < records.length(); i++)
      {
         RSConnectDeploymentRecord record = records.get(i);
         deployments_.put(record.getUrl(), record);
      }
   }
   
   private final RSConnect connect_;
   
   private ThemedButton deployButton_;
   private CheckBox launchCheck_;
   private RSConnectAccount defaultAccount_;
   
   // Map of app URL to the deployment made to that URL
   private Map<String, RSConnectDeploymentRecord> deployments_ = 
         new HashMap<String, RSConnectDeploymentRecord>();
}