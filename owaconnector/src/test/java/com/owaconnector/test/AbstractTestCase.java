package com.owaconnector.test;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/applicationContext.xml"})
@TransactionConfiguration(transactionManager="transactionManager", defaultRollback=false)

public class AbstractTestCase extends AbstractTransactionalJUnit4SpringContextTests{

}
