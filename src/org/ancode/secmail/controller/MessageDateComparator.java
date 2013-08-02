
package org.ancode.secmail.controller;

import java.util.Comparator;

import org.ancode.secmail.mail.Message;

public class MessageDateComparator implements Comparator<Message> {
    public int compare(Message o1, Message o2) {
        try {
            if (o1.getSentDate() == null) {
                return 1;
            } else if (o2.getSentDate() == null) {
                return -1;
            } else
                return o2.getSentDate().compareTo(o1.getSentDate());
        } catch (Exception e) {
            return 0;
        }
    }
}
