import { Button, Layout, Space, Nav } from "@douyinfe/semi-ui";
import {
  IconMoon,
  IconSun,
  IconSemiLogo,
  IconGithubLogo,
} from "@douyinfe/semi-icons";
import { useState } from "react";

export const MyHeader = () => {
  const { Header } = Layout;

  const [theme, setTheme] = useState<"light" | "dark">("light");

  const switchMode = () => {
    const body = document.body;
    if (body.hasAttribute("theme-mode")) {
      body.removeAttribute("theme-mode");
      setTheme("light");
    } else {
      body.setAttribute("theme-mode", "dark");
      setTheme("dark");
    }
  };

  return (
    <Header>
      <Nav
        mode={"horizontal"}
        items={[]}
        header={{
          logo: <IconSemiLogo style={{ height: "36px", fontSize: 36 }} />,
          text: "无印",
        }}
        footer={
          <Space>
            <Button
              onClick={switchMode}
              theme="borderless"
              icon={
                theme === "light" ? (
                  <IconMoon size="extra-large" />
                ) : (
                  <IconSun size="extra-large" />
                )
              }
            />
            <Button
              onClick={() => {
                window.open("https://github.com/LauZzL/no-watermark");
              }}
              theme="borderless"
              icon={<IconGithubLogo size="extra-large" />}
            />
          </Space>
        }
      ></Nav>
    </Header>
  );
};
