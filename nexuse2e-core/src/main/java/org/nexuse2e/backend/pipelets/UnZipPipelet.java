/**
 * NEXUSe2e Business Messaging Open Source
 * Copyright 2000-2021, direkt gruppe GmbH
 * <p>
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation version 3 of
 * the License.
 * <p>
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.nexuse2e.backend.pipelets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nexuse2e.Engine;
import org.nexuse2e.NexusException;
import org.nexuse2e.messaging.AbstractPipelet;
import org.nexuse2e.messaging.MessageContext;
import org.nexuse2e.pojo.ConversationPojo;
import org.nexuse2e.pojo.MessagePayloadPojo;
import org.nexuse2e.pojo.MessagePojo;
import org.nexuse2e.pojo.PartnerPojo;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author mbreilmann
 */
public class UnZipPipelet extends AbstractPipelet {

    private static Logger LOG = LogManager.getLogger(UnZipPipelet.class);

    /**
     * @param args
     */
    public static void main(String[] args) {

        if (args.length != 1) {
            System.err.println("No file specified!");
            return;
        }

        FileInputStream fis;
        try {
            fis = new FileInputStream(args[0]);

            byte[] payloadData = new byte[fis.available()];
            fis.read(payloadData);
            fis.close();

            MessageContext messageContext = new MessageContext();
            MessagePojo messagePojo = new MessagePojo();
            ConversationPojo conversationPojo = new ConversationPojo();
            messagePojo.setConversation(conversationPojo);
            PartnerPojo partnerPojo = new PartnerPojo();
            partnerPojo.setPartnerId("TestPartner");
            conversationPojo.setPartner(partnerPojo);
            MessagePayloadPojo messagePayloadPojo = new MessagePayloadPojo();
            messagePayloadPojo.setPayloadData(payloadData);

            messageContext.setMessagePojo(messagePojo);
            messagePayloadPojo.setMessage(messagePojo);
            messagePojo.getMessagePayloads().add(messagePayloadPojo);

            new UnZipPipelet().processMessage(messageContext);

            for (Iterator<MessagePayloadPojo> payloads =
                 messageContext.getMessagePojo().getMessagePayloads().iterator(); payloads.hasNext(); ) {
                MessagePayloadPojo tempMessagePayloadPojo = payloads.next();
                /*
                FileOutputStream fos = new FileOutputStream( "/Volumes/myby/temp/unzip_output.dat" );
                fos.write( tempMessagePayloadPojo.getPayloadData() );
                fos.flush();
                fos.close();
                */

                System.out.println("Mime type: " + tempMessagePayloadPojo.getMimeType());
                System.out.println("Payload (" + tempMessagePayloadPojo.getSequenceNumber() + "):\n" + new String(tempMessagePayloadPojo.getPayloadData()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Done!");

    }

    /* (non-Javadoc)
     * @see org.nexuse2e.messaging.Pipelet#processMessage(org.nexuse2e.messaging.MessageContext)
     */
    public MessageContext processMessage(MessageContext messageContext) throws NexusException {

        final int BUFFER = 4096;

        String mimeType = "unknown";
        int count = 0;
        byte data[] = new byte[BUFFER];

        // one zipped payload can contain multiple payloads (combined mode)
        // we need the ability to add new payloads, so we better replace all
        // to keep the order of apearance and not mess with the iterator
        List<MessagePayloadPojo> newPayloads = new ArrayList<MessagePayloadPojo>();

        MessagePojo messagePojo = messageContext.getMessagePojo();
        for (MessagePayloadPojo messagePayloadPojo : messagePojo.getMessagePayloads()) {
            byte payloadData[] = messagePayloadPojo.getPayloadData();

            // Test if ZIP file  - seach for PKZIP header 0x04034b50
            if ((payloadData[0] == 0x50) && (payloadData[1] == 0x4B) && (payloadData[2] == 0x03) && (payloadData[3] == 0x04)) {
                try {
                    ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(payloadData));
                    ZipEntry zipEntry = null;

                    while ((zipEntry = zis.getNextEntry()) != null) {

                        String fileName = zipEntry.getName();

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        BufferedOutputStream bos = new BufferedOutputStream(baos, BUFFER);
                        while ((count = zis.read(data, 0, BUFFER)) != -1) {
                            bos.write(data, 0, count);
                        }
                        bos.flush();
                        bos.close();

                        zis.closeEntry();

                        MessagePayloadPojo newPayload = (MessagePayloadPojo) messagePayloadPojo.clone();
                        newPayload.setPayloadData(baos.toByteArray());
                        newPayload.setMessage(messagePojo);
                        newPayload.setContentId(fileName);
                        if ((Engine.getInstance() != null) && (fileName != null)) {
                            mimeType = Engine.getInstance().getMimeFromFileName(fileName);
                        }
                        newPayload.setMimeType(mimeType);
                        newPayloads.add(newPayload);

                        LOG.debug("Uncompressed size of file '" + fileName + "': " + newPayload.getPayloadData().length);

                    }

                    zis.close();

                } catch (IOException e) {
                    throw new NexusException("Error decompressing message payload.", e);
                } catch (CloneNotSupportedException e) {
                    throw new NexusException("Error decompressing message payload.", e);
                }
            } else {
                LOG.info("Message payload not a ZIP file!");
            }
        }

        // replace payloads
        messagePojo.setMessagePayloads(newPayloads);

        return messageContext;
    }

}
