import json
import os

def create_version_directories():
    """
    读取 ../settings.json 中的版本信息，
    为每个版本创建文件夹和空的 gradle.properties 文件
    """
    # 读取 settings.json 文件
    settings_path = os.path.join("..", "settings.json")
    
    try:
        with open(settings_path, 'r', encoding='utf-8') as f:
            settings = json.load(f)
        
        versions = settings.get("versions", [])
        
        # 为每个版本创建目录和文件
        for version in versions:
            # 创建版本目录（如果不存在）
            if not os.path.exists(version):
                os.makedirs(version)
                print(f"创建目录: {version}")
            
            # 创建 gradle.properties 文件（如果不存在）
            gradle_properties_path = os.path.join(version, "gradle.properties")
            if not os.path.exists(gradle_properties_path):
                with open(gradle_properties_path, 'w') as f:
                    pass  # 创建空文件
                print(f"创建文件: {gradle_properties_path}")
            else:
                print(f"文件已存在，跳过: {gradle_properties_path}")
                
    except FileNotFoundError:
        print(f"错误: 找不到文件 {settings_path}")
    except json.JSONDecodeError:
        print("错误: 无法解析 JSON 文件")
    except Exception as e:
        print(f"错误: {e}")

if __name__ == "__main__":
    create_version_directories()