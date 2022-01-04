package quartz;

import java.text.ParseException;
import java.util.Map;
import java.util.Properties;

import org.jetbrains.annotations.NotNull;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import utils.LogUtil;

/**
 * 任务管理，请参考文档 http://www.quartz-scheduler.org/documentation
 *
 * @author dengerYang
 * @date 2021年12月27日
 */
public class QuartzManager {

    private Scheduler sched = null;
    private String instanceName;

    private QuartzManager() {
    }

    private QuartzManager(Scheduler sched, String instanceName) {
        this.sched = sched;
        this.instanceName = instanceName;
    }

    /**
     * 初始化
     *
     * @param instanceName 实例名称，根据此名称识别任务资源。
     */
    public static QuartzManager getInstance(String instanceName) {
        try {
            return getInstance(instanceName, 1); //线程能满足资源即可
        } catch (SchedulerException e) {
            e.printStackTrace();
            throw new RuntimeException("QuartzManager初始化失败" + instanceName, e);
        }
    }

    public static QuartzManager getInstance(String instanceName, int threadCount) throws SchedulerException {
        Properties props = new Properties();
        props.put("org.quartz.scheduler.instanceName", instanceName);
        props.put("org.quartz.threadPool.threadCount", threadCount + "");
        StdSchedulerFactory stdSchedulerFactory = new StdSchedulerFactory(props);
        return new QuartzManager(stdSchedulerFactory.getScheduler(), instanceName);
    }

    /**
     * 检查是否符合表达式
     *
     * @param cronExpression 表达式
     * @return true表达式正确
     */
    public static boolean checkCronExpression(String cronExpression) {
        try {
            new CronExpression(cronExpression);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * 添加一个主定时任务，如果存在会替换。注意，只有运行一个任务，每次执行都会清除掉之前任务
     *
     * @param clazz          jobCLass
     * @param cronExpression cron表达式，支持;分隔
     * @param dataMap        传递给job的参数
     */
    public void runJob(Class<? extends Job> clazz, @NotNull String cronExpression, Map<? extends String, ?> dataMap) {
        try {
            sched.clear();
            String[] split = cronExpression.split(";");
            for (int i = 0; i < split.length; i++) {
                String cron = split[i];
                JobDetail detail = JobBuilder.newJob(clazz).withIdentity(instanceName + i).build();
                if (dataMap != null && !dataMap.isEmpty()) {
                    detail.getJobDataMap().putAll(dataMap);
                }
                LogUtil.info("创建任务 [ " + cron + " ] " + dataMap.get(HandlerJob.KEY_HANDLER).getClass().getSimpleName());
                sched.scheduleJob(detail, TriggerBuilder.newTrigger().withSchedule(CronScheduleBuilder.cronSchedule(cron)).build());
            }
            // 启动
            if (!sched.isShutdown()) {
                sched.start();
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
            throw new RuntimeException("添加定时任务失败", e);
        }
    }

    public void stopJob() {
        try {
            sched.clear(); // 清除资源
            sched.shutdown();
        } catch (SchedulerException e) {
            e.printStackTrace();
            throw new RuntimeException("停止定时任务失败", e);
        }
    }
}
