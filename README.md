# spring-batch-practice

## Job

![spring-batch-job](./image/spring-batch-job.png)

![jobinstance-jobexecution.png](./image/jobinstance-jobexecution.png)

### JobLauncher

![spring-batch-job-component-relation.png](./image/spring-batch-job-component-relation.png)

- 잡을 실행하는 역할
- 잡의 재실행 가능 여부 검증
- 잡의 실행 방법
- 파라미터 유효성 검증

### JobRepository

- 청크의 처리가 스텝 내에서 완료되면 JobRepository 내부에 있는 JobExecution 또는 StepExecution 에 상태를 갱신
- 청크를 처리할 때마다 커밋 수, 시작 및 종료 시간, 기타 정보가 있는 StepExecution 의 스텝 상태 업데이트

### JobInstance

- 스프링 배치 잡의 논리적인 실행, 잡의 이름과 잡의 논리적인 실행을 위해 제공되는 고유한 식별 파라미터 모음  
- 잡이 실행될 때마다 새로운 JobInstance 생성  
- 실패한 잡을 재식작하면 JobInstance 은 생성되지 않음

### JobExecution

- 스프링 배치 잡의 실제 실행  
- 잡을 구동할 때마다 새로운 JobExecution 생성
- 잡이 실패한 이후 다시 실행해도 새로운 JobExecution 생성

### StepExecution

- 스텝의 실제 실행
- JobExecution 은 StepExecution 여러 개와 연관

## 스텝

잡을 구성하는 독립된 작업의 단위  
태스크릿(tasklet)과 청크(chunk) 기반의 두 가지 스텝 유형 존재

- 태스크릿(tasklet)
    - 스텝이 중지될 때까지 `execute` 메서드가 반복해서 수행
    - 초기화, 저장 프로시저 실행, 알림 전송등에 많이 사용
- 청크(chunk)
    - 아이템 기반의 처리에 사용
    - 3개의 주요 부분으로 구성될 수 있음
        - `ItemReader` : 데이터를 읽어옴
        - `ItemProcessor` : 데이터를 가공(선택)
        - `ItemWriter` : 데이터를 저장

