/*
 * Copyright (C) 2014 SERPRO - Serviço Federal de Processamento de Dados
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package br.gov.frameworkdemoiselle.scheduler.internal.bootstrap;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.internal.implementation.AnnotatedMethodProcessor;
import br.gov.frameworkdemoiselle.scheduler.lifecycle.Schedule;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.NameQualifier;
import br.gov.frameworkdemoiselle.util.Reflections;
import br.gov.frameworkdemoiselle.util.ResourceBundle;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import static org.quartz.TriggerBuilder.newTrigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;

/**
 *
 * @author 70744416353
 */
public abstract class AbstractLifecycleScheduler<A extends Annotation> implements Extension {

    private Class<A> annotationClass;

    @SuppressWarnings("rawtypes")
    private List<AnnotatedMethodProcessor> processors = Collections
            .synchronizedList(new ArrayList<AnnotatedMethodProcessor>());

    private transient static ResourceBundle bundle;

    Scheduler scheduler;

    protected abstract Logger getLogger();

    private final static String DEMOISELLE_JOB = "DEMOISELLE_JOB";

    protected static ResourceBundle getBundle() {
        if (bundle == null) {
            bundle = Beans.getReference(ResourceBundle.class, new NameQualifier("demoiselle-core-bundle"));
        }

        return bundle;
    }

    protected <T> AnnotatedMethodProcessor<T> newProcessorInstance(AnnotatedMethod<T> annotatedMethod) {

        return new AnnotatedMethodProcessor<T>(annotatedMethod);
    }

    protected Class<A> getAnnotationClass() {
        if (this.annotationClass == null) {
            this.annotationClass = Reflections.getGenericTypeArgument(this.getClass(), 0);
        }

        return this.annotationClass;
    }

    @SuppressWarnings("unchecked")
    public <T> void processAnnotatedType(@Observes final ProcessAnnotatedType<T> event) {

        final AnnotatedType<T> annotatedType = event.getAnnotatedType();

        for (AnnotatedMethod<?> am : annotatedType.getMethods()) {
            if (am.isAnnotationPresent(getAnnotationClass())) {
                processors.add(newProcessorInstance((AnnotatedMethod<T>) am));
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected synchronized void proccessEvent() {

        getLogger().debug(getBundle().getString("executing-all", getAnnotationClass().getSimpleName()));

        Collections.sort(processors);
        Exception failure = null;

        for (Iterator<AnnotatedMethodProcessor> iter = processors.iterator(); iter.hasNext();) {
            AnnotatedMethodProcessor<?> processor = iter.next();

            try {

                Schedule schedule = processor.getAnnotatedMethod().getAnnotation(Schedule.class);

                if (schedule == null) {
                    return;
                }

                Class<Serializable> jobClass = (Class<Serializable>) processor.getAnnotatedMethod().getDeclaringType().getJavaClass().asSubclass(Serializable.class);

                if (jobClass == null) {
                    return;
                }

                JobDetail job = newJob(CdiJob.class).usingJobData(DEMOISELLE_JOB, jobClass.getName()).build();
                Trigger trigger = newTrigger().withSchedule(cronSchedule(schedule.cron())).build();
                scheduler.scheduleJob(job, trigger);

            } catch (Exception cause) {
                failure = cause;
            }

        }

        if (failure != null) {
            throw new DemoiselleException(failure);
        }

    }

    public void initScheduler(@Observes BeforeBeanDiscovery event) {
        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
        } catch (SchedulerException ex) {
            getLogger().error(ex.getMessage());
        }
    }

    public void startScheduler(@Observes AfterDeploymentValidation event) {
        try {
            scheduler.start();
            getLogger().info(scheduler.getMetaData().getSummary());
        } catch (SchedulerException se) {
            throw new RuntimeException(se);
        }
    }

    public void shutdownScheduler(@Observes BeforeShutdown event) {
        try {
            scheduler.shutdown(true);
        } catch (SchedulerException se) {
            throw new RuntimeException(se);
        }
    }

}
