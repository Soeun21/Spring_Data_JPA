package study.datajpa.entity;

import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
public class JpaBaseEntity {

    @Column(updatable = false) // 값을 실수로 바꿔도 변경되지 않게 설정
    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    @PrePersist // persist하기 전에 이벤트가 발생한다
    public void prePersist(){
        LocalDateTime now = LocalDateTime.now();
        createdDate = now;
        updatedDate = now;
    }

    @PreUpdate  //
    public void preUpdate(){
        updatedDate = LocalDateTime.now();
    }



}
