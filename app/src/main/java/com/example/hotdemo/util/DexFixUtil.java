package com.example.hotdemo.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.example.hotdemo.test.DexFixTest;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * @author : zlrab
 * @version : V1.0
 * @ClassName : DexFixUtil.java
 * @date : 28/08/19 下午 02:22
 * @Description : dex注入流程
 * 1.移动.dex文件,将dex移动至app私有目录
 * 2.反射从PathClassLoader中取得旧的Element数组（oldElements）
 * 3.构建DexClassLoader,反射取得其中新的Element数组(newElement)
 * 4.构建新Element数组(finallyElement)用于合并上两步获得的Element数组，将newElement置于新数组的最前端，实现热修复
 * 5.反射替换oldElement对象的引用地址，将其指向finallyElement
 */
public class DexFixUtil {
    protected static final String TAG = "ZLRab";

    protected static final String APP_DATA_HOTDEX_PATH = "hotdex";

    private DexFixUtil() {

    }

    public static DexFixUtil getInstance() {
        return Instance.sDexFixUtil;
    }

    private static class Instance {
        private static DexFixUtil sDexFixUtil = new DexFixUtil();
    }

    /**
     * 入口
     *
     * @param context 当前app上下文
     */
    private void repair(Context context) {

        String localDexPath = Environment.getExternalStorageDirectory() + File.separator + "zlrab";

        DexFixUtil.MoveFileResultBean[] moveFileResultBeans = DexFixUtil.getInstance().moveDexsToData(localDexPath, context);

        for (DexFixUtil.MoveFileResultBean bean : moveFileResultBeans) {
            Log.d(TAG, "repair: bean===" + bean.toString());
        }

        Object dexPathListObject = DexFixUtil.getInstance().getDexPathListObject((PathClassLoader) context.getClassLoader());

        Object oldDexElementArrObject = DexFixUtil.getInstance().getDexElementArrObject(dexPathListObject);

        File hotdex = context.getDir("hotdex", Context.MODE_PRIVATE);

        List<File> dataDexList = new ArrayList<>();
        DexFixUtil.getInstance().recursiveFindDexFile(hotdex, dataDexList);

        for (int index = 0, size = dataDexList.size(); index < size; index++) {

            DexClassLoader dexClassLoader = new DexClassLoader(dataDexList.get(index).getAbsolutePath(), context.getDir("opt_dex", Context.MODE_PRIVATE).getAbsolutePath(), null, context.getClassLoader());

            Object pathListObject = DexFixUtil.getInstance().getDexPathListObject(dexClassLoader);

            Object dexElementObject = DexFixUtil.getInstance().getDexElementArrObject(pathListObject);

            Log.d(TAG, "repair:dexElementObject " + Array.getLength(dexElementObject));

            oldDexElementArrObject = DexFixUtil.getInstance().mergeElement(oldDexElementArrObject, dexElementObject);
        }

        Object finallyDexPathList = DexFixUtil.getInstance().getDexPathListObject(context.getClassLoader());

        DexFixUtil.getInstance().setClassField(finallyDexPathList, finallyDexPathList.getClass(), "dexElements", oldDexElementArrObject);

        Log.d(TAG, "repair: " + Array.getLength(oldDexElementArrObject));

        Log.d(TAG, "repair: " + context.getClassLoader().toString());

        try {
            Object o = context.getClassLoader().loadClass("com.example.hotdemo.test.DexFixTest").newInstance();
            if (o instanceof DexFixTest) {
                DexFixTest dexFixTest = (DexFixTest) o;
                dexFixTest.testFix(context);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取DexPathList对象
     *
     * @param baseDexClassLoaderObject DexClassLoader Object or PathClassLoader Object
     * @return 返回反射获得的pathList对象
     */
    public Object getDexPathListObject(@NotNull Object baseDexClassLoaderObject) {
        return getClassField(baseDexClassLoaderObject, baseDexClassLoaderObject.getClass().getSuperclass(), "pathList");
    }

    /**
     * 获取Element[]对象
     *
     * @param dexPathListObject DexPathList Object
     * @return 返回反射获得的Element[]对象
     */
    public Object getDexElementArrObject(@NotNull Object dexPathListObject) {
        return getClassField(dexPathListObject, dexPathListObject.getClass(), "dexElements");
    }

    /**
     * 合并新旧element
     * 默认newElement会在新数组的最前面
     *
     * @param oldElement 旧的element
     * @param newElement 新的element
     * @return 合并后的新数组
     */
    public Object mergeElement(@NotNull Object oldElement, @NotNull Object newElement) {
        Class<?> clazz = oldElement.getClass().getComponentType();
        int oldLength = Array.getLength(oldElement);
        int newLength = Array.getLength(newElement);
        int finallyLength = oldLength + newLength;
        Object finallyElements = Array.newInstance(clazz, finallyLength);

        for (int index = 0; index < finallyLength; index++) {
            if (index < newLength) {
                Array.set(finallyElements, index, Array.get(newElement, index));
            } else {
                Array.set(finallyElements, index, Array.get(oldElement, index - newLength));
            }
            //  Array.set(finallyElements, index, Array.get(index < newLength ? newElement : oldElement, index < newLength ? index : index - newLength));
        }
        return finallyElements;
    }

    /**
     * 反射修改类中成员变量指向的引用地址
     *
     * @param classObject 需要被反射的类的对象
     * @param clazz       需要被反射的字节码文件
     * @param fieldName   字段名字
     * @param newObject   替换的新对象
     */
    public void setClassField(@NotNull Object classObject, Class<?> clazz, @NotNull String fieldName, @NotNull Object newObject) {
        try {
            Field declaredField = clazz.getDeclaredField(fieldName);
            declaredField.setAccessible(true);
            declaredField.set(classObject, newObject);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 反射获取类中成员变量
     *
     * @param classObject 需要被反射的类的对象
     * @param clazz       需要被反射的字节码文件
     * @param fieldName   字段名字
     * @return 返回类实例中的成员变量对象
     */
    public Object getClassField(@NotNull Object classObject, Class<?> clazz, @NotNull String fieldName) {

        Object object = null;

        try {
            Field declaredField = clazz.getDeclaredField(fieldName);
            declaredField.setAccessible(true);
            object = declaredField.get(classObject);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return object;
    }


    /**
     * 批量移动指定目录下所有.dex文件到app私有目录
     *
     * @param dexsAbsolutePath 需要被检索的目录
     * @param context          用于获取当前app私有目录的上下文
     * @return 移动成功与否结果数组
     */
    public MoveFileResultBean[] moveDexsToData(@NotNull String dexsAbsolutePath, Context context) {

        File file = new File(dexsAbsolutePath);

        if (!file.isDirectory()) {
            Log.e(TAG, "moveDexsToData: dexAbsolutePath is not a directory , this method is used to import dex in batches ,you can call moveDexToData(@NonNull String dexAbsolutePath, Context context)");
            return null;
        }

        List<File> dexFileList = new ArrayList<>();

        recursiveFindDexFile(file, dexFileList);

        MoveFileResultBean[] results = new MoveFileResultBean[dexFileList.size()];

        for (int index = 0, size = dexFileList.size(); index < size; index++) {
            File tempDexFile = dexFileList.get(index);
            boolean moveResult = moveDexToData(tempDexFile, context);
            results[index] = new MoveFileResultBean(tempDexFile, moveResult);
        }
        return results;
    }

    public boolean moveDexToData(@NotNull String dexAbsolutePath, Context context) {
        return moveDexToData(new File(dexAbsolutePath), context);
    }

    /**
     * 移动单个.dex文件到app私有目录，因为dex只有保存在私有目录下才会得到执行
     * 默认会放在私有目录的APP_DATA_HOTDEX_PATH(hotdex)目录下（/data/data/appPackageName/hotdex/）
     *
     * @param file    需要被移动的.dex文件
     * @param context 用于获取当前app私有目录的上下文
     * @return 保存成功与失败
     */
    public boolean moveDexToData(File file, Context context) {

        if (!file.exists() || !file.getName().startsWith("classes") || !file.getName().endsWith(".dex")) {
            Log.e(TAG, "moveDexToData: dexAbsolutePath directory file does not exist or dexAbsolutePath directory file not .dex file");
            return false;
        }

        File hotdexFile = context.getDir(APP_DATA_HOTDEX_PATH, Context.MODE_PRIVATE);

        String appDataHotDexPath = hotdexFile.getAbsolutePath() + File.separator + file.getName();

        Log.d(TAG, "moveDexToData: " + appDataHotDexPath);
        deleteFile(appDataHotDexPath);

        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;

        try {
            inputStream = new FileInputStream(file.getAbsolutePath());
            fileOutputStream = new FileOutputStream(appDataHotDexPath);
            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = inputStream.read(bytes)) != -1) {
                fileOutputStream.write(bytes, 0, len);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileItExists(appDataHotDexPath);
    }

    /**
     * 递归读取指定目录下所有.dex文件
     *
     * @param file        需要被扫描的目录
     * @param dexFileList 用来保存结果集
     */
    public void recursiveFindDexFile(File file, List<File> dexFileList) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File tempFile : files) {
                    recursiveFindDexFile(tempFile, dexFileList);
                }
            }
        } else {
            if (file.getName().startsWith("class") && file.getName().endsWith(".dex")) {
                dexFileList.add(file);
            }
        }
    }

    /**
     * 测试路径文件是否存在
     *
     * @param fileAbsolutePath 文件绝对路径
     * @return true  or false
     */
    public boolean fileItExists(@NotNull String fileAbsolutePath) {
        File file = new File(fileAbsolutePath);
        return file.exists();
    }

    /**
     * 删除指定文件
     *
     * @param fileAbsolutePath 文件绝对路径
     */
    public void deleteFile(@NotNull String fileAbsolutePath) {
        File file = new File(fileAbsolutePath);
        if (fileItExists(fileAbsolutePath)) {
            boolean delete = file.delete();
        }
    }

    public static class MoveFileResultBean {
        private File file;
        private boolean moveResult;

        public MoveFileResultBean() {
        }

        public MoveFileResultBean(File file, boolean moveResult) {
            this.file = file;
            this.moveResult = moveResult;
        }

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }

        public boolean isMoveResult() {
            return moveResult;
        }

        public void setMoveResult(boolean moveResult) {
            this.moveResult = moveResult;
        }

        @Override
        public String toString() {
            return "FileBean{" +
                    "file=" + file.getAbsolutePath() +
                    ", moveResult=" + moveResult +
                    '}';
        }
    }
}
