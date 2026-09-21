-- =============================================================
-- 人力资源管理服务平台 hr-manage-platform
-- 数据库建表脚本（MySQL 8.0）
-- 字符集：utf8mb4 / 排序：utf8mb4_general_ci
-- 约定：
--   1) 所有表含审计字段 create_time / update_time / is_deleted（逻辑删除）
--   2) 金额统一 DECIMAL(10,2)；日期 DATE；时间 TIME/DATETIME
--   3) 状态/枚举用 TINYINT，业务层用常量解释
--   4) 不使用外键约束（逻辑外键），便于演示与级联控制
--   5) 本脚本只建表 + 角色/菜单/权限初始数据
--      管理员账号(admin/123456) 由后端阶段④的初始化器写入（保证 BCrypt 哈希正确）
-- =============================================================

DROP DATABASE IF EXISTS hr_manage;
CREATE DATABASE hr_manage DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE hr_manage;

-- ---------------------------
-- 1. 系统用户
-- ---------------------------
CREATE TABLE sys_user (
    id              BIGINT       PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    username        VARCHAR(50)  NOT NULL COMMENT '登录账号',
    password        VARCHAR(100) NOT NULL DEFAULT '' COMMENT 'BCrypt 密码哈希',
    real_name       VARCHAR(50)  DEFAULT '' COMMENT '真实姓名',
    avatar          VARCHAR(255) DEFAULT '' COMMENT '头像 URL',
    email           VARCHAR(100) DEFAULT '' COMMENT '邮箱',
    phone           VARCHAR(20)  DEFAULT '' COMMENT '手机号',
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    employee_id     BIGINT       DEFAULT NULL COMMENT '关联员工档案(hr_employee.id)，普通员工必填',
    last_login_time DATETIME     DEFAULT NULL COMMENT '最后登录时间',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
    UNIQUE KEY uk_username (username),
    KEY idx_employee (employee_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统用户表';

-- ---------------------------
-- 2. 角色
-- ---------------------------
CREATE TABLE sys_role (
    id          BIGINT      PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    role_key    VARCHAR(50) NOT NULL COMMENT '角色标识 admin/hr/employee',
    role_name   VARCHAR(50) NOT NULL COMMENT '角色名称',
    remark      VARCHAR(200) DEFAULT '' COMMENT '备注',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted  TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
    UNIQUE KEY uk_role_key (role_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色表';

-- ---------------------------
-- 3. 菜单 / 按钮权限
--    menu_type: 1=目录/菜单  2=按钮(权限点)
--    perms: 权限标识，用于后端鉴权与前端按钮显隐
-- ---------------------------
CREATE TABLE sys_menu (
    id         BIGINT      PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    parent_id  BIGINT      NOT NULL DEFAULT 0 COMMENT '父菜单ID,0为顶级',
    menu_name  VARCHAR(50) NOT NULL COMMENT '菜单名称',
    menu_type  TINYINT     NOT NULL DEFAULT 1 COMMENT '1菜单 2按钮',
    perms      VARCHAR(100) DEFAULT NULL COMMENT '权限标识',
    path       VARCHAR(200) DEFAULT '' COMMENT '前端路由路径',
    component  VARCHAR(200) DEFAULT '' COMMENT '前端组件',
    icon       VARCHAR(50)  DEFAULT '' COMMENT '图标',
    sort       INT         NOT NULL DEFAULT 0 COMMENT '排序',
    create_time DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
    KEY idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='菜单权限表';

-- ---------------------------
-- 4. 用户角色关联
-- ---------------------------
CREATE TABLE sys_user_role (
    id         BIGINT   PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id    BIGINT   NOT NULL COMMENT '用户ID',
    role_id    BIGINT   NOT NULL COMMENT '角色ID',
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户角色关联表';

-- ---------------------------
-- 5. 角色菜单关联（授权）
-- ---------------------------
CREATE TABLE sys_role_menu (
    id       BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    role_id  BIGINT NOT NULL COMMENT '角色ID',
    menu_id  BIGINT NOT NULL COMMENT '菜单ID',
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    KEY idx_menu (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色菜单关联表';

-- ---------------------------
-- 6. 部门（树形，parent_id 自关联）
-- ---------------------------
CREATE TABLE hr_department (
    id         BIGINT      PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    dept_name  VARCHAR(50) NOT NULL COMMENT '部门名称',
    parent_id  BIGINT      NOT NULL DEFAULT 0 COMMENT '上级部门ID,0为顶级',
    dept_code  VARCHAR(50) DEFAULT '' COMMENT '部门编码',
    leader_id  BIGINT      DEFAULT NULL COMMENT '负责人(关联 hr_employee.id)',
    sort       INT         NOT NULL DEFAULT 0 COMMENT '排序',
    status     TINYINT     NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    create_time DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
    KEY idx_parent (parent_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='部门表';

-- ---------------------------
-- 7. 岗位（隶属部门）
-- ---------------------------
CREATE TABLE hr_position (
    id         BIGINT      PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    post_name  VARCHAR(50) NOT NULL COMMENT '岗位名称',
    post_code  VARCHAR(50) DEFAULT '' COMMENT '岗位编码',
    dept_id    BIGINT      NOT NULL DEFAULT 0 COMMENT '所属部门',
    headcount  INT         NOT NULL DEFAULT 0 COMMENT '编制人数',
    remark     VARCHAR(200) DEFAULT '' COMMENT '岗位描述',
    status     TINYINT     NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    create_time DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
    KEY idx_dept (dept_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='岗位表';

-- ---------------------------
-- 8. 员工档案（业务主表）
--    status: 1试用 2在职 3离职
--    gender: 1男 2女
-- ---------------------------
CREATE TABLE hr_employee (
    id           BIGINT      PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    emp_code     VARCHAR(30) NOT NULL COMMENT '工号',
    name         VARCHAR(50) NOT NULL COMMENT '姓名',
    gender       TINYINT     NOT NULL DEFAULT 1 COMMENT '1男 2女',
    phone        VARCHAR(20) DEFAULT '' COMMENT '手机号',
    email        VARCHAR(100) DEFAULT '' COMMENT '邮箱',
    id_card      VARCHAR(20) DEFAULT '' COMMENT '身份证号',
    dept_id      BIGINT      NOT NULL DEFAULT 0 COMMENT '部门',
    post_id      BIGINT      NOT NULL DEFAULT 0 COMMENT '岗位',
    job_level    VARCHAR(20) DEFAULT '' COMMENT '职级',
    entry_date   DATE        DEFAULT NULL COMMENT '入职日期',
    regular_date DATE        DEFAULT NULL COMMENT '转正日期',
    status       TINYINT     NOT NULL DEFAULT 1 COMMENT '1试用 2在职 3离职',
    education    VARCHAR(20) DEFAULT '' COMMENT '学历',
    avatar       VARCHAR(255) DEFAULT '' COMMENT '头像 URL',
    bank_card    VARCHAR(30) DEFAULT '' COMMENT '工资卡号',
    address      VARCHAR(255) DEFAULT '' COMMENT '住址',
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted   TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
    UNIQUE KEY uk_emp_code (emp_code),
    KEY idx_dept (dept_id),
    KEY idx_name (name),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='员工档案表';

-- ---------------------------
-- 9. 考勤记录（员工+日期 唯一）
--    status: NORMAL正常 LATE迟到 EARLY早退 ABSENT旷工 LEAVE请假 OVERTIME加班
--    leave_type: 1事假 2病假 3年假
-- ---------------------------
CREATE TABLE hr_attendance (
    id              BIGINT      PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    employee_id     BIGINT      NOT NULL COMMENT '员工ID',
    emp_name        VARCHAR(50) DEFAULT '' COMMENT '姓名快照',
    dept_id         BIGINT      DEFAULT NULL COMMENT '部门快照',
    attend_date     DATE        NOT NULL COMMENT '考勤日期',
    clock_in        TIME        DEFAULT NULL COMMENT '上班打卡',
    clock_out       TIME        DEFAULT NULL COMMENT '下班打卡',
    status          VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '考勤状态',
    leave_type      TINYINT     DEFAULT NULL COMMENT '请假类型 1事假 2病假 3年假',
    overtime_hours  DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT '加班小时数',
    remark          VARCHAR(255) DEFAULT '' COMMENT '备注',
    create_time     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted      TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
    UNIQUE KEY uk_emp_date (employee_id, attend_date),
    KEY idx_date (attend_date),
    KEY idx_dept (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考勤记录表';

-- ---------------------------
-- 10. 薪资档案（每员工一份）
-- ---------------------------
CREATE TABLE hr_salary_standard (
    id             BIGINT      PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    employee_id    BIGINT      NOT NULL COMMENT '员工ID',
    base_salary    DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '基本工资',
    post_salary    DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '岗位工资',
    perf_salary    DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '绩效工资',
    social_base    DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '社保缴纳基数',
    fund_base      DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '公积金缴纳基数',
    bank_card      VARCHAR(30) DEFAULT '' COMMENT '工资卡号',
    create_time    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
    UNIQUE KEY uk_emp (employee_id),
    KEY idx_salary (base_salary)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='薪资档案表';

-- ---------------------------
-- 11. 工资单（员工+月份 唯一，快照关键字段）
-- ---------------------------
CREATE TABLE hr_payroll (
    id               BIGINT      PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    employee_id      BIGINT      NOT NULL COMMENT '员工ID',
    salary_month     CHAR(7)     NOT NULL COMMENT '发薪月份 2026-09',
    emp_name         VARCHAR(50) DEFAULT '' COMMENT '姓名快照',
    dept_name        VARCHAR(50) DEFAULT '' COMMENT '部门名称快照',
    post_name        VARCHAR(50) DEFAULT '' COMMENT '岗位名称快照',
    base_salary      DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '基本工资',
    post_salary      DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '岗位工资',
    perf_salary      DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '绩效工资',
    overtime_pay     DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '加班费',
    attendance_deduct DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '考勤扣款(正数表示扣)',
    gross_pay        DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '应发工资',
    social_personal  DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '社保个人',
    fund_personal   DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '公积金个人',
    tax              DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '个人所得税',
    net_pay          DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '实发工资',
    remark           VARCHAR(255) DEFAULT '' COMMENT '备注',
    create_time      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted       TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
    UNIQUE KEY uk_emp_month (employee_id, salary_month),
    KEY idx_month (salary_month),
    KEY idx_dept_name (dept_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='工资单表';

-- ---------------------------
-- 12. 登录日志（演示 AOP 日志切面）
-- ---------------------------
CREATE TABLE sys_login_log (
    id          BIGINT      PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(50) DEFAULT '' COMMENT '登录账号',
    ip          VARCHAR(64) DEFAULT '' COMMENT '登录IP',
    login_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    status      TINYINT     NOT NULL DEFAULT 1 COMMENT '1成功 0失败',
    msg         VARCHAR(255) DEFAULT '' COMMENT '结果说明',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='登录日志表';


-- =============================================================
-- 初始数据：角色、菜单权限、角色授权
-- =============================================================

-- 1) 三个内置角色
INSERT INTO sys_role (id, role_key, role_name, remark) VALUES
(1, 'admin',  '系统管理员', '拥有全部权限，负责系统配置'),
(2, 'hr',     '人事专员', '负责人事业务，不含系统权限配置'),
(3, 'employee','普通员工', '仅查看个人相关信息');

-- 2) 菜单与按钮权限（id 显式指定，便于后面授权引用）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perms, path, component, icon, sort) VALUES
(1,  0, '系统管理',   1, 'system:view',         '/system',         'Layout',        'setting', 1),
(2,  1, '用户管理',   1, 'system:user:view',    '/system/user',    'system/user',   'user',    1),
(3,  2, '新增用户',   2, 'system:user:add',     '',               '',              '',        1),
(4,  1, '角色管理',   1, 'system:role:view',    '/system/role',    'system/role',   'role',    2),
(5,  4, '分配权限',   2, 'system:role:edit',    '',               '',              '',        1),
(6,  0, '组织管理',   1, 'org:view',            '/org',            'Layout',        'org',     2),
(7,  6, '部门管理',   1, 'org:dept:view',       '/org/dept',       'org/dept',     'dept',    1),
(8,  7, '部门编辑',   2, 'org:dept:edit',       '',               '',              '',        1),
(9,  6, '岗位管理',   1, 'org:post:view',       '/org/post',       'org/post',     'post',    2),
(10, 9, '岗位编辑',   2, 'org:post:edit',       '',               '',              '',        1),
(11, 0, '员工管理',   1, 'emp:view',            '/employee',       'employee',      'peoples', 3),
(12, 11,'员工新增编辑',2, 'emp:edit',            '',               '',              '',        1),
(13, 0, '考勤管理',   1, 'att:view',            '/attendance',     'attendance',    'clock',   4),
(14, 13,'考勤录入',   2, 'att:edit',            '',               '',              '',        1),
(15, 0, '薪酬管理',   1, 'sal:view',            '/salary',         'salary',        'money',   5),
(16, 15,'薪资档案编辑',2, 'sal:standard:edit',  '',               '',              '',        1),
(17, 15,'工资单生成', 2, 'sal:payroll:edit',    '',               '',              '',        1),
(18, 0, '数据看板',   1, 'dashboard:view',      '/dashboard',      'dashboard',     'chart',   6),
(19, 1, '菜单管理',   1, 'system:menu:view',    '/system/menu',    'system/menu',   'menu',    3),
(20, 19,'菜单编辑',   2, 'system:menu:edit',    '',                '',              '',        1);

-- 3) 角色授权
-- 管理员：全部
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;

-- 人事：除「系统管理」相关(系统管理目录及其子菜单)外全部
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, id FROM sys_menu
WHERE perms NOT IN ('system:view', 'system:user:view', 'system:role:view',
                   'system:user:add', 'system:role:edit', 'system:user:edit');

-- 普通员工：仅看板、员工、考勤、薪酬四个业务页（数据按当前用户过滤只显示本人）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 3, id FROM sys_menu
WHERE perms IN ('dashboard:view', 'emp:view', 'att:view', 'sal:view');

-- =============================================================
-- 说明：
--   * 管理员账号(admin / 123456) 与角色绑定在阶段④由后端初始化器写入，
--     避免 SQL 中写死 BCrypt 哈希导致与代码算法不一致。
--   * 部门、岗位、员工、考勤、薪资档案、工资单 等演示数据在阶段⑦联调前
--     通过后端批量接口或初始化脚本导入，保证看板有数据可展示。
-- =============================================================
