package xenosoft.imldintelligence.module.identity.internal.repository.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xenosoft.imldintelligence.module.identity.internal.model.SmsVerificationCode;

@Mapper
public interface SmsVerificationCodeMapper extends BaseMapper<SmsVerificationCode> {
}

