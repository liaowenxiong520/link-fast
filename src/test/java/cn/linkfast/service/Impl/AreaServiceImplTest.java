package cn.linkfast.service.Impl;

import cn.linkfast.config.AppConfig;
import cn.linkfast.service.AreaService;
import cn.linkfast.vo.AreaVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AppConfig.class})
public class AreaServiceImplTest {

    @Autowired
    private AreaService areaService;

    /**
     * 测试获取全部地域信息（不传 codes）
     */
    @Test
    public void testQueryAreaTreeAll() throws Exception {
        System.out.println("========== 测试获取全部地域树 ==========");

        List<AreaVO> areaTree = areaService.queryAreaTree(null);

        assertNotNull(areaTree, "返回结果不应为 null");
        assertFalse(areaTree.isEmpty(), "返回结果不应为空");

        System.out.println("顶级地域数量: " + areaTree.size());

        // 格式化输出 JSON，方便查看树形结构
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        String json = mapper.writeValueAsString(areaTree);
        System.out.println("地域树形数据：");
        System.out.println(json);
    }

    /**
     * 测试获取指定地域信息（传入 codes）
     */
    @Test
    public void testQueryAreaTreeByCodes() throws Exception {
        System.out.println("========== 测试获取指定地域树（US, CN） ==========");

        List<AreaVO> areaTree = areaService.queryAreaTree(Arrays.asList("US", "CN"));

        assertNotNull(areaTree, "返回结果不应为 null");

        System.out.println("返回地域数量: " + areaTree.size());

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        String json = mapper.writeValueAsString(areaTree);
        System.out.println("地域树形数据：");
        System.out.println(json);
    }
}

