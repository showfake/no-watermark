import { Layout } from "@douyinfe/semi-ui";
import { MyHeader } from "../components/MyHeader";
import { Parser } from "../components/Parser";


export const Main = () => {
  const { Footer, Content } = Layout;

  return (
    <Layout className="w-full h-full flex">
      <MyHeader />
      <Content className="w-full h-full flex justify-center mt-[40px] overflow-auto">
        <Parser />
      </Content>
      <Footer className="text-gray line-height-[64px] text-align-center">
        Copyright © 2025 无印
      </Footer>
    </Layout>
  );
};
