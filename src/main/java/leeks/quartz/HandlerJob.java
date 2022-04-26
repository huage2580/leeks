package leeks.quartz;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.intellij.util.ExceptionUtil;
import leeks.constant.Constants;
import leeks.handler.AbstractHandler;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import leeks.utils.LogUtil;

/**
 * 执行时钟任务，不想定义那么多类了，所以写了一个共用的，后面有特殊在各个单独处理。<br/>
 * 使用时请注意，将KEY_HANDLER和KEY_CODES，放入参数中
 * <pre>
 *                 QuartzManager quartzManager = new QuartzManager("实例名称");
 *                 HashMap<String, Object> dataMap = new HashMap<>();
 *                 dataMap.put(HandlerJob.KEY_HANDLER, leeks.handler);
 *                 dataMap.put(HandlerJob.KEY_CODES, codes);
 *                 quartzManager.runJob(HandlerJob.class, instance.getValue("key_cron_expression_stock"), dataMap); // 添加任务并执行
 * </pre>
 * 请参考文档 http://www.quartz-scheduler.org/documentation
 *
 * @author dengerYang
 * @date 2021年12月27日
 */
public class HandlerJob implements Job {
    public static final String KEY_HANDLER = "handler";
    public static final String KEY_CODES = "codes";


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap mergedJobDataMap = context.getMergedJobDataMap();
        Object handler = mergedJobDataMap.get(KEY_HANDLER);

        if (handler instanceof AbstractHandler) {
            Constants.EXECUTOR_SERVICE.submit(() -> {
                try {
                    List<String> codes = (List<String>) mergedJobDataMap.get(KEY_CODES);
                    ((AbstractHandler) handler).handle(codes);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    LogUtil.info(String.format("%s 运行 %s ;下一次运行时间为 %s",
                            simpleDateFormat.format(new Date()),
                            handler.getClass().getSimpleName(),
                            simpleDateFormat.format(context.getNextFireTime())));
                } catch (Exception e) {
                    LogUtil.info("刷新出现异常：" + ExceptionUtil.getMessage(e) + "\r\n" + ExceptionUtil.currentStackTrace());
                    throw new RuntimeException(e);
                }
            });
        } else {
            LogUtil.info("刷新出现异常：handler为" + handler.getClass().getCanonicalName());
        }
    }
}
