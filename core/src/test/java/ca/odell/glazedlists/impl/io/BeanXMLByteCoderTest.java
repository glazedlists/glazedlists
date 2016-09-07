/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.io;

import ca.odell.glazedlists.io.ByteCoder;

import java.io.IOException;

import javax.swing.JLabel;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests the BeanXMLByteCoder..
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class BeanXMLByteCoderTest {

    /**
     * Tests that the XML encoding works.
     */
    @Test
    public void testCoding() throws IOException {
        Bufferlo data = new Bufferlo();

        JLabel bean = new JLabel();
        bean.setText("Limp Bizkit");
        bean.setToolTipText("Fred Durst");
        bean.setEnabled(false);

        ByteCoder beanXMLByteCoder = new BeanXMLByteCoder();
        beanXMLByteCoder.encode(bean, data.getOutputStream());
        JLabel beanCopy = (JLabel)beanXMLByteCoder.decode(data.getInputStream());

        assertEquals(bean.getText(), beanCopy.getText());
        assertEquals(bean.getToolTipText(), beanCopy.getToolTipText());
        assertEquals(bean.isEnabled(), beanCopy.isEnabled());
    }
}
