package com.ceitechs.domain.service.service;

import lombok.Getter;
import lombok.Setter;

/**
 * @author iddymagohe on 8/20/16.
 * @since 1.0
 */

@Getter
@Setter
public class EmailModel<T> {


    /**
     * one or many receivers of this email.
     * use where it's ok for the receivers to see others email address.
     */
    private String[] recipients = {};


    private String[] copiedRecipients = {};

    /**
     * should be used for mailing list kind of a situation.
     * Where concealing recipients ids is essential
     */
    private String[] bccRecipients = {};

    private String subject;

    /**
     * for sending template email(HTML) and want to pass data values for properties like FirstName , lastName etc
     * Set a domain object with these value data, you must implement accessor(get) methods of these properties
     * when template is not provide , model is used as message body(String)
     */
    private T model;

    /**
     * to send HTML like emails. this file is expected to be in resources/template
     * this is a template file name without file extension. Ex.accountActivationMessage should be passed for accountActivationMessage.vm
     */
    private String template;

}
