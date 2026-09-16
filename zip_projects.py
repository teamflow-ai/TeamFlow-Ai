import os
import zipfile

def zip_dir(dir_path, zip_path, exclude_dirs):
    with zipfile.ZipFile(zip_path, 'w', zipfile.ZIP_DEFLATED) as zipf:
        for root, dirs, files in os.walk(dir_path):
            dirs[:] = [d for d in dirs if d not in exclude_dirs]
            for file in files:
                file_path = os.path.join(root, file)
                arcname = os.path.relpath(file_path, os.path.dirname(dir_path))
                zipf.write(file_path, arcname)

if __name__ == '__main__':
    base_dir = r"E:\Purvesh TeamFlow Ai"
    
    frontend_dir = os.path.join(base_dir, "frontend")
    backend_dir = os.path.join(base_dir, "teamflow-ai")
    
    frontend_zip = os.path.join(base_dir, "teamflow-frontend-final.zip")
    backend_zip = os.path.join(base_dir, "teamflow-backend-final.zip")
    
    excludes = ['node_modules', 'target', '.git', 'dist', '.idea', '.vscode']
    
    print("Zipping frontend...")
    zip_dir(frontend_dir, frontend_zip, excludes)
    
    print("Zipping backend...")
    zip_dir(backend_dir, backend_zip, excludes)
    
    print("Done!")
