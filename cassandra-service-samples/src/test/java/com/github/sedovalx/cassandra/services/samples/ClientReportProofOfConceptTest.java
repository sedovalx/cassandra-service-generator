package com.github.sedovalx.cassandra.services.samples;

import com.github.sedovalx.cassandra.services.samples.config.CassandraConfig;
import com.github.sedovalx.cassandra.services.samples.config.ServicesConfig;
import com.github.sedovlax.cassandra.services.samples.ClientReport;
import com.github.sedovlax.cassandra.services.samples.services.ClientReportService;
import org.cassandraunit.spring.CassandraDataSet;
import org.cassandraunit.spring.CassandraUnitTestExecutionListener;
import org.cassandraunit.spring.EmbeddedCassandra;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {CassandraConfig.class, ServicesConfig.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, CassandraUnitTestExecutionListener.class})
@EmbeddedCassandra(configuration = "test-cassandra.yaml")
@CassandraDataSet(keyspace = "sample", value = { "client_report.cql" })
public class ClientReportProofOfConceptTest {

    // make it lazy to give the cassandra-unit process time to start
    // in real life you don't need it usually
    @Lazy
    @Autowired
    private ClientReportService service;

    @After
    public void tearDown() throws Exception {
        // delete all reports after each test
        service.accessor().deleteAll();
    }

    @Before
    public void setUp() throws Exception {
        // create a report in the store
        ClientReport report1 = new ClientReport();
        report1.setRegion(13);
        report1.setTemplateCode("1234566578");
        report1.setPeriodYear(2015);
        report1.setPeriodCode(1);
        report1.setClientId(777L);
        report1.setData("some data");
        report1.setDeleted(false);
        service.mapper().save(report1);

        // and another one
        ClientReport report2 = new ClientReport();
        report2.setRegion(22);
        report2.setTemplateCode("1234566578");
        report2.setPeriodYear(2016);
        report2.setPeriodCode(5);
        report2.setClientId(444L);
        report2.setData("another data");
        report2.setDeleted(false);
        service.mapper().save(report2);
    }

    @Test
    public void testAccessorCanReadData() {
        // expect that all 2 reports are returned
        Assert.assertEquals(2, service.accessor().getAll().all().size());
    }

    @Test
    public void testAccessorMapperInteroperability(){
        List<ClientReport> reports = service.accessor().get(13).all();
        ClientReport report = reports.get(0);
        String expectedData = "expected data";
        // update data via the accessor
        service.accessor().updateData(
            expectedData,
            report.getRegion(),
            report.getTemplateCode(),
            report.getPeriodYear(),
            report.getPeriodCode(),
            report.getClientId()
        );
        // get it via the mapper
        Optional<ClientReport> actualReport = service.mapper().get(
            report.getRegion(),
            report.getTemplateCode(),
            report.getPeriodYear(),
            report.getPeriodCode(),
            report.getClientId()
        );
        Assert.assertTrue(actualReport.isPresent());
        Assert.assertEquals(expectedData, actualReport.get().getData());
    }

    @Test
    public void testDeleteData() {
        ClientReport report = new ClientReport(73, "1234566577", 2016, 5, 444L, "data", new Date(), false);
        ClientReport report2 = new ClientReport(73, "1234566577", 2014, 5, 444L, "data", new Date(), false);
        ClientReport report3 = new ClientReport(74, "1234566578", 2016, 5, 444L, "data", new Date(), false);
        service.mapper().save(report);
        service.mapper().save(report2);
        service.mapper().save(report3);

        Assert.assertEquals(2, service.accessor().get(73).all().size());
        service.accessor().deleteByPartitionKey(73);
        Assert.assertEquals(0, service.accessor().get(73).all().size());
        Assert.assertEquals(1, service.accessor().get(74).all().size());
    }

}
