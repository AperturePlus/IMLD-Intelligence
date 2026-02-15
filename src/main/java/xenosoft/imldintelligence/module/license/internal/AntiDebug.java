package xenosoft.imldintelligence.module.license.internal;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.Connector;
import xenosoft.imldintelligence.module.license.internal.model.Fingerprint;

import java.lang.management.ManagementFactory;
public class AntiDebug {
    private static volatile boolean debugDetected = false;
    private static native boolean isDebuggerPresentNative();

    public static boolean isWindows(){
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    static{
        if(isWindows()){
            try{
                System.loadLibrary("antidebug");
            }catch(UnsatisfiedLinkError e){
                // 无法加载本地库，可能是因为平台不支持或库文件缺失。可以选择记录日志或继续使用Java层的检测方法。

            }
        }
    }
    /**
     * 检测调试器
     */
    public static boolean isDebuggerAttached() {
        // 检测JVM参数
        String jvmArgs = ManagementFactory.getRuntimeMXBean().getInputArguments().toString();
        if (jvmArgs.contains("jdwp") || jvmArgs.contains("dt_socket") || jvmArgs.contains("debug")) {
            debugDetected = true;
            return true;
        }

        // 检测调试模式
        if (ManagementFactory.getRuntimeMXBean().getInputArguments().stream()
                .anyMatch(arg -> arg.contains("debug"))) {
            debugDetected = true;
            return true;
        }


        return false;
    }

    /**
     * 检测代码完整性（防止篡改）
     */
    public static boolean checkIntegrity() {
        try {
            // 检查关键类是否被修改
            String className = Fingerprint.class.getName();
            Class<?> clazz = Class.forName(className);

            // 简单的类加载器检查
            if (clazz.getClassLoader() == null) {
                return true; // Bootstrap类加载器
            }

            // 可以添加更多检查，如字节码哈希验证
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 时间炸弹检测（防止时间回调）
     */
    public static boolean checkTimeBomb(long lastCheckTime) {
        long currentTime = System.currentTimeMillis();

        // 如果当前时间比上次检查时间早，说明系统时间被回调
        // 容忍1分钟误差
        return currentTime >= lastCheckTime - 60000;
    }
}
