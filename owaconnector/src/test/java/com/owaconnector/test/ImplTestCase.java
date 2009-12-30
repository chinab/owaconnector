package com.owaconnector.test;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.TransactionConfiguration;

@ContextConfiguration(locations={"impl-context.xml"})
@TransactionConfiguration(transactionManager="transactionManager", defaultRollback=false)

public class ImplTestCase extends AbstractTestCase {

}
